package itau.gateway.queue.application.service;

import java.util.concurrent.TimeUnit;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import itau.gateway.queue.domain.model.pagamento.Pagamento;
import itau.gateway.queue.domain.model.pagamento.PagamentoMessage;
import itau.gateway.queue.domain.port.in.PagamentoUseCase;
import itau.gateway.queue.domain.port.out.PaymentRepositoryPort;
import itau.gateway.queue.infrastructure.config.IdGenerator;
import itau.pix.commons.enums.StatusPagamento;
import itau.pix.commons.messaging.RabbitMQConstants;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class PagamentoUseCaseImpl implements PagamentoUseCase {

    private final IdGenerator idGenerator;
    private final PaymentRepositoryPort paymentRepository;
    private final RabbitTemplate rabbitTemplate;

    private final Cache<String, Boolean> transactionCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .build();

    public PagamentoUseCaseImpl(IdGenerator idGenerator, PaymentRepositoryPort paymentRepository, RabbitTemplate rabbitTemplate) {
        this.idGenerator = idGenerator;
        this.paymentRepository = paymentRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void processPayment(Pagamento paymentRequest) {
        // Assinatura para evitar duplicidade acidental (2 minutos)
        String transactionSignature = String.format("%s-%s-%s",
                paymentRequest.getSenderAccount(),
                paymentRequest.getReceiverPixKey(),
                paymentRequest.getAmount().toPlainString());

        if (transactionCache.getIfPresent(transactionSignature) != null) {
            System.out.println("⚠️ AVISO: Transação idêntica detectada nos últimos 2 minutos.");
            return;
        }

        // Definição do ID (Idempotência)
        String paymentId = (paymentRequest.getId() == null || paymentRequest.getId().isEmpty())
                ? idGenerator.generateId()
                : paymentRequest.getId();

        // Verifica se ID já existe no banco
        if (paymentRepository.findById(paymentId).isPresent()) {
            System.out.println("ID de pagamento já processado: " + paymentId);
            return;
        }

        // Se passou, bloqueia no cache e prossegue
        transactionCache.put(transactionSignature, true);

        Pagamento paymentEntity = new Pagamento();
        paymentEntity.setId(paymentId);
        paymentEntity.setAmount(paymentRequest.getAmount());
        paymentEntity.setSenderAccount(paymentRequest.getSenderAccount());
        paymentEntity.setReceiverPixKey(paymentRequest.getReceiverPixKey());
        paymentEntity.setStatus(StatusPagamento.PROCESSANDO);

        saveAsync(paymentEntity);

        PagamentoMessage paymentMessage = new PagamentoMessage();
        paymentMessage.setId(paymentId);
        paymentMessage.setAmount(paymentRequest.getAmount());
        paymentMessage.setSenderAccount(paymentRequest.getSenderAccount());
        paymentMessage.setReceiverPixKey(paymentRequest.getReceiverPixKey());

        rabbitTemplate.convertAndSend(RabbitMQConstants.FILA_PAGAMENTO, paymentMessage);
        System.out.println("Payment with ID " + paymentId + " sent to queue.");
    }

    @Async
    public void saveAsync(Pagamento paymentEntity) {
        paymentRepository.save(paymentEntity);
    }
}
