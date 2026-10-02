package itau.persistence.queue.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import itau.persistence.queue.domain.model.ChavePix;
import itau.persistence.queue.domain.model.Pagamento;
import itau.persistence.queue.domain.model.PagamentoMessage;
import itau.persistence.queue.domain.port.out.ChavePixRepositoryPort;
import itau.persistence.queue.domain.port.out.PagamentoRepositoryPort;
import itau.pix.commons.enums.StatusPagamento;

class PagamentoPersistenceUseCaseImplTest {

    private static final String PAGAMENTO_ID = "pagamento-1";
    private static final String CONTA_REMETENTE = "12345679";
    private static final String CHAVE_DESTINATARIO = "12345678802";

    private InMemoryChavePixRepository chavePixRepository;
    private InMemoryPagamentoRepository pagamentoRepository;
    private PagamentoPersistenceUseCaseImpl useCase;
    private ChavePix destinatario;

    @BeforeEach
    void setUp() {
        chavePixRepository = new InMemoryChavePixRepository();
        pagamentoRepository = new InMemoryPagamentoRepository();
        useCase = new PagamentoPersistenceUseCaseImpl(pagamentoRepository, chavePixRepository);

        chavePixRepository.save(chavePix("12345679801", "1234", CONTA_REMETENTE, "1000.00"));
        destinatario = chavePixRepository.save(chavePix(CHAVE_DESTINATARIO, "1235", "12345678", "0.00"));
        pagamentoRepository.save(new Pagamento(
                PAGAMENTO_ID, new BigDecimal("200.00"), CONTA_REMETENTE, CHAVE_DESTINATARIO,
                StatusPagamento.PROCESSANDO, null));
    }

    @Test
    void aplicaATransferenciaQuandoOPagamentoAindaEstaProcessando() {
        useCase.persist(mensagem(), true);

        assertThat(saldoDoRemetente()).isEqualByComparingTo("800.00");
        assertThat(saldoDoDestinatario()).isEqualByComparingTo("200.00");
        assertThat(statusDoPagamento()).isEqualTo(StatusPagamento.SUCESSO);
    }

    @Test
    void reentregaDaMesmaMensagemDeSucessoNaoMovimentaOSaldoDeNovo() {
        useCase.persist(mensagem(), true);
        useCase.persist(mensagem(), true);

        assertThat(saldoDoRemetente()).isEqualByComparingTo("800.00");
        assertThat(saldoDoDestinatario()).isEqualByComparingTo("200.00");
        assertThat(statusDoPagamento()).isEqualTo(StatusPagamento.SUCESSO);
    }

    @Test
    void mensagemDeFalhaTardiaNaoSobrescrevePagamentoJaConcluidoComSucesso() {
        useCase.persist(mensagem(), true);
        useCase.persist(mensagem(), false);

        assertThat(statusDoPagamento()).isEqualTo(StatusPagamento.SUCESSO);
    }

    @Test
    void mensagemDeSucessoTardiaNaoReativaPagamentoQueJaFalhou() {
        useCase.persist(mensagem(), false);
        useCase.persist(mensagem(), true);

        assertThat(statusDoPagamento()).isEqualTo(StatusPagamento.FALHOU);
        assertThat(saldoDoRemetente()).isEqualByComparingTo("1000.00");
        assertThat(saldoDoDestinatario()).isEqualByComparingTo("0.00");
    }

    @Test
    void saldoInsuficienteNaoMovimentaNadaEMarcaOPagamentoComoFalhou() {
        useCase.persist(mensagemDeValor("1500.00"), true);

        assertThat(saldoDoRemetente()).isEqualByComparingTo("1000.00");
        assertThat(saldoDoDestinatario()).isEqualByComparingTo("0.00");
        assertThat(statusDoPagamento()).isEqualTo(StatusPagamento.FALHOU);
    }

    @Test
    void estornaODebitoQuandoNaoConsegueCreditarODestinatario() {
        chavePixRepository.recusarCreditosPara(destinatario.getId());

        useCase.persist(mensagem(), true);

        assertThat(saldoDoRemetente()).isEqualByComparingTo("1000.00");
        assertThat(saldoDoDestinatario()).isEqualByComparingTo("0.00");
        assertThat(statusDoPagamento()).isEqualTo(StatusPagamento.FALHOU);
    }

    private PagamentoMessage mensagem() {
        return mensagemDeValor("200.00");
    }

    private PagamentoMessage mensagemDeValor(String valor) {
        return new PagamentoMessage(PAGAMENTO_ID, new BigDecimal(valor), CONTA_REMETENTE, CHAVE_DESTINATARIO, null, 0, null);
    }

    private BigDecimal saldoDoRemetente() {
        return chavePixRepository.findByNumeroConta(CONTA_REMETENTE).orElseThrow().getSaldo();
    }

    private BigDecimal saldoDoDestinatario() {
        return chavePixRepository.findByValorChave(CHAVE_DESTINATARIO).orElseThrow().getSaldo();
    }

    private StatusPagamento statusDoPagamento() {
        return pagamentoRepository.findById(PAGAMENTO_ID).orElseThrow().getStatus();
    }

    private static ChavePix chavePix(String valorChave, String agencia, String conta, String saldo) {
        return ChavePix.builder()
                .id(UUID.randomUUID())
                .tipoChave("cpf")
                .valorChave(valorChave)
                .tipoConta("corrente")
                .numeroAgencia(agencia)
                .numeroConta(conta)
                .nomeCorrentista("Teste")
                .saldo(new BigDecimal(saldo))
                .build();
    }

    // Os fakes guardam e devolvem cópias, como o adapter do Mongo faz ao mapear Document <-> domínio:
    // alterar o objeto lido sem chamar save() não pode "vazar" para o armazenamento.
    private static final class InMemoryChavePixRepository implements ChavePixRepositoryPort {

        private final Map<UUID, ChavePix> store = new HashMap<>();
        private final Set<UUID> chavesQueRecusamCredito = new HashSet<>();

        @Override
        public Optional<ChavePix> findByValorChave(String valorChave) {
            return store.values().stream()
                    .filter(c -> valorChave.equals(c.getValorChave()))
                    .findFirst()
                    .map(InMemoryChavePixRepository::copy);
        }

        @Override
        public Optional<ChavePix> findByConta(String numeroAgencia, String numeroConta) {
            return store.values().stream()
                    .filter(c -> numeroAgencia.equals(c.getNumeroAgencia()) && numeroConta.equals(c.getNumeroConta()))
                    .findFirst()
                    .map(InMemoryChavePixRepository::copy);
        }

        @Override
        public Optional<ChavePix> findByNumeroConta(String numeroConta) {
            return store.values().stream()
                    .filter(c -> numeroConta.equals(c.getNumeroConta()))
                    .findFirst()
                    .map(InMemoryChavePixRepository::copy);
        }

        @Override
        public Optional<ChavePix> findById(UUID id) {
            return Optional.ofNullable(store.get(id)).map(InMemoryChavePixRepository::copy);
        }

        @Override
        public boolean debitarSeHouverSaldo(UUID chaveId, BigDecimal valor) {
            ChavePix chave = store.get(chaveId);
            if (chave == null || chave.getSaldo().compareTo(valor) < 0) {
                return false;
            }
            chave.setSaldo(chave.getSaldo().subtract(valor));
            return true;
        }

        @Override
        public boolean creditar(UUID chaveId, BigDecimal valor) {
            ChavePix chave = store.get(chaveId);
            if (chave == null || chavesQueRecusamCredito.contains(chaveId)) {
                return false;
            }
            chave.setSaldo(chave.getSaldo().add(valor));
            return true;
        }

        // Preparação dos testes: não faz parte da porta, que propositalmente não expõe save().
        ChavePix save(ChavePix chavePix) {
            store.put(chavePix.getId(), copy(chavePix));
            return copy(chavePix);
        }

        void recusarCreditosPara(UUID chaveId) {
            chavesQueRecusamCredito.add(chaveId);
        }

        private static ChavePix copy(ChavePix c) {
            return ChavePix.builder()
                    .id(c.getId())
                    .tipoChave(c.getTipoChave())
                    .valorChave(c.getValorChave())
                    .tipoConta(c.getTipoConta())
                    .numeroAgencia(c.getNumeroAgencia())
                    .numeroConta(c.getNumeroConta())
                    .nomeCorrentista(c.getNomeCorrentista())
                    .sobrenomeCorrentista(c.getSobrenomeCorrentista())
                    .saldo(c.getSaldo())
                    .dataHoraInclusao(c.getDataHoraInclusao())
                    .dataHoraInativacao(c.getDataHoraInativacao())
                    .status(c.getStatus())
                    .build();
        }
    }

    private static final class InMemoryPagamentoRepository implements PagamentoRepositoryPort {

        private final Map<String, Pagamento> store = new HashMap<>();

        @Override
        public Pagamento save(Pagamento pagamento) {
            store.put(pagamento.getId(), copy(pagamento));
            return copy(pagamento);
        }

        @Override
        public Optional<Pagamento> findById(String id) {
            return Optional.ofNullable(store.get(id)).map(InMemoryPagamentoRepository::copy);
        }

        private static Pagamento copy(Pagamento p) {
            return new Pagamento(p.getId(), p.getAmount(), p.getSenderAccount(), p.getReceiverPixKey(),
                    p.getStatus(), p.getErrorDescription());
        }
    }
}
