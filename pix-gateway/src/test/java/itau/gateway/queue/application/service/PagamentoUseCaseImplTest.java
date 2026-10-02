package itau.gateway.queue.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import itau.gateway.queue.domain.exception.UnprocessableEntityException;
import itau.gateway.queue.domain.model.pagamento.IdempotencyRecord;
import itau.gateway.queue.domain.model.pagamento.Pagamento;
import itau.gateway.queue.domain.model.pagamento.PagamentoMessage;
import itau.gateway.queue.domain.port.out.IdempotencyRepositoryPort;
import itau.gateway.queue.domain.port.out.PaymentMessageSenderPort;
import itau.gateway.queue.domain.port.out.PaymentRepositoryPort;
import itau.pix.commons.enums.StatusPagamento;

class PagamentoUseCaseImplTest {

    private InMemoryIdempotencyRepository idempotencyRepository;
    private InMemoryPaymentRepository paymentRepository;
    private RecordingMessageSender messageSender;
    private PagamentoUseCaseImpl useCase;

    @BeforeEach
    void setUp() {
        idempotencyRepository = new InMemoryIdempotencyRepository();
        paymentRepository = new InMemoryPaymentRepository();
        messageSender = new RecordingMessageSender();
        AtomicInteger sequencia = new AtomicInteger();
        useCase = new PagamentoUseCaseImpl(
                () -> "pagamento-" + sequencia.incrementAndGet(), paymentRepository, idempotencyRepository, messageSender);
    }

    @Test
    void primeiraRequisicaoGravaOPagamentoEPublicaUmaVez() {
        String id = useCase.processPayment(pagamento("200.00"), "chave-1");

        assertThat(id).isEqualTo("pagamento-1");
        assertThat(paymentRepository.get(id).getStatus()).isEqualTo(StatusPagamento.PROCESSANDO);
        assertThat(messageSender.enviadas).hasSize(1);
        assertThat(messageSender.enviadas.get(0).getId()).isEqualTo(id);
    }

    @Test
    void requisicaoRepetidaComAMesmaChaveDevolveOMesmoPagamentoSemPublicarDeNovo() {
        String primeiro = useCase.processPayment(pagamento("200.00"), "chave-1");
        String segundo = useCase.processPayment(pagamento("200.00"), "chave-1");

        assertThat(segundo).isEqualTo(primeiro);
        assertThat(messageSender.enviadas).hasSize(1);
        assertThat(paymentRepository.quantidade()).isEqualTo(1);
    }

    @Test
    void mesmaChaveComOutroPagamentoEhRejeitada() {
        useCase.processPayment(pagamento("200.00"), "chave-1");

        assertThatThrownBy(() -> useCase.processPayment(pagamento("999.00"), "chave-1"))
                .isInstanceOf(UnprocessableEntityException.class);
        assertThat(messageSender.enviadas).hasSize(1);
    }

    @Test
    void doisPagamentosIguaisComChavesDiferentesSaoAmbosAceitos() {
        String primeiro = useCase.processPayment(pagamento("200.00"), "chave-1");
        String segundo = useCase.processPayment(pagamento("200.00"), "chave-2");

        assertThat(segundo).isNotEqualTo(primeiro);
        assertThat(messageSender.enviadas).hasSize(2);
    }

    @Test
    void valorEquivalenteComEscalaDiferenteContaComoOMesmoPagamento() {
        String primeiro = useCase.processPayment(pagamento("200"), "chave-1");
        String segundo = useCase.processPayment(pagamento("200.00"), "chave-1");

        assertThat(segundo).isEqualTo(primeiro);
    }

    @Test
    void falhaNaPublicacaoLiberaAChaveEMarcaOPagamentoComoFalhou() {
        messageSender.falharNaProximaPublicacao();

        assertThatThrownBy(() -> useCase.processPayment(pagamento("200.00"), "chave-1"))
                .isInstanceOf(IllegalStateException.class);

        assertThat(idempotencyRepository.contem("chave-1")).isFalse();
        assertThat(paymentRepository.get("pagamento-1").getStatus()).isEqualTo(StatusPagamento.FALHOU);

        String id = useCase.processPayment(pagamento("200.00"), "chave-1");

        assertThat(id).isEqualTo("pagamento-2");
        assertThat(messageSender.enviadas).hasSize(1);
    }

    private static Pagamento pagamento(String valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setSenderAccount("12345679");
        pagamento.setReceiverPixKey("12345678802");
        pagamento.setAmount(new BigDecimal(valor));
        return pagamento;
    }

    private static final class InMemoryIdempotencyRepository implements IdempotencyRepositoryPort {

        private final Map<String, IdempotencyRecord> store = new ConcurrentHashMap<>();

        @Override
        public Optional<IdempotencyRecord> registrarSeNova(IdempotencyRecord record) {
            return Optional.ofNullable(store.putIfAbsent(record.key(), record));
        }

        @Override
        public void liberar(String key) {
            store.remove(key);
        }

        boolean contem(String key) {
            return store.containsKey(key);
        }
    }

    private static final class InMemoryPaymentRepository implements PaymentRepositoryPort {

        private final Map<String, Pagamento> store = new HashMap<>();

        @Override
        public void save(Pagamento pagamento) {
            Pagamento copia = new Pagamento();
            copia.setId(pagamento.getId());
            copia.setAmount(pagamento.getAmount());
            copia.setSenderAccount(pagamento.getSenderAccount());
            copia.setReceiverPixKey(pagamento.getReceiverPixKey());
            copia.setStatus(pagamento.getStatus());
            copia.setRetryCount(pagamento.getRetryCount());
            store.put(pagamento.getId(), copia);
        }

        Pagamento get(String id) {
            return store.get(id);
        }

        int quantidade() {
            return store.size();
        }
    }

    private static final class RecordingMessageSender implements PaymentMessageSenderPort {

        private final List<PagamentoMessage> enviadas = new ArrayList<>();
        private boolean falharNaProxima;

        @Override
        public void sendPaymentMessage(PagamentoMessage paymentMessage) {
            if (falharNaProxima) {
                falharNaProxima = false;
                throw new IllegalStateException("broker indisponível");
            }
            enviadas.add(paymentMessage);
        }

        void falharNaProximaPublicacao() {
            falharNaProxima = true;
        }
    }
}
