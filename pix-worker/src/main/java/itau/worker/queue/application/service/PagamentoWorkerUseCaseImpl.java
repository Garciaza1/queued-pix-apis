package itau.worker.queue.application.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import itau.pix.commons.enums.StatusPagamento;
import itau.worker.queue.domain.port.in.PagamentoWorkerUseCase;
import itau.worker.queue.application.port.validator.PagamentoValidator;
import itau.worker.queue.domain.model.ChavePix;
import itau.worker.queue.domain.model.PagamentoMessage;
import itau.worker.queue.domain.port.out.ChavePixRepositoryPort;

@Service
public class PagamentoWorkerUseCaseImpl implements PagamentoWorkerUseCase {

    private final RabbitTemplate rabbitTemplate;
    private final ChavePixRepositoryPort chavePixRepository;
    private final Queue paymentQueue;
    private final Queue failedQueue;
    private final Queue successQueue;
    private final PagamentoValidator validator;

    public PagamentoWorkerUseCaseImpl(
            @Lazy RabbitTemplate rabbitTemplate,
            ChavePixRepositoryPort chavePixRepository,
            @Qualifier("paymentQueue") Queue paymentQueue,
            @Qualifier("failedPaymentQueue") Queue failedQueue,
            @Qualifier("successPaymentQueue") Queue successQueue,
            PagamentoValidator validator
    ) {
        this.rabbitTemplate = rabbitTemplate;
        this.chavePixRepository = chavePixRepository;
        this.paymentQueue = paymentQueue;
        this.failedQueue = failedQueue;
        this.successQueue = successQueue;
        this.validator = validator;
    }

    @Override
    public void process(PagamentoMessage message) {
        if (message == null) {
            System.out.println("Mensagem nula recebida pelo worker.");
            return;
        }

        System.out.println("Processing payment: " + message.getId());

        Optional<ChavePix> senderOpt = chavePixRepository.findByNumeroConta(message.getSenderAccount());
        Optional<ChavePix> receiverOpt = chavePixRepository.findByValorChave(message.getReceiverPixKey());

        // usar validator para todas as regras
        var result = validator.validate(message, senderOpt, receiverOpt);
        if (!result.isValid()) {
            message.setErrorDescription(result.getError());
            handleFailed(message);
            return;
        }

        // se chegou aqui, validações ok -> enviar sucesso
        handleSuccess(message);
    }

    private void handleFailed(PagamentoMessage message) {
        String messageId = Objects.requireNonNull(message.getId(), "ID da mensagem não pode ser nulo");
        if (message.getRetryCount() < 3) {
            message.setRetryCount(message.getRetryCount() + 1);
            // ao reenfileirar, manter status null (indica pendente) e enviar para fila principal
            message.setStatus(null);
            rabbitTemplate.convertAndSend(paymentQueue.getName(), message, new CorrelationData(messageId));
            System.out.println("🔄 Retrying payment " + message.getId() + " attempt " + message.getRetryCount() + " - reason: " + message.getErrorDescription());
        } else {
            message.setStatus(StatusPagamento.FALHOU);
            rabbitTemplate.convertAndSend(failedQueue.getName(), message, new CorrelationData(messageId));
            System.out.println("❌ Payment " + message.getId() + " failed after 3 attempts: " + message.getErrorDescription());
        }
    }

    private void handleSuccess(PagamentoMessage message) {
        String messageId = Objects.requireNonNull(message.getId(), "ID da mensagem não pode ser nulo");
        message.setStatus(StatusPagamento.SUCESSO);
        rabbitTemplate.convertAndSend(successQueue.getName(), message, new CorrelationData(messageId));
        System.out.println("✅ Payment " + message.getId() + " validated successfully");
    }
}
