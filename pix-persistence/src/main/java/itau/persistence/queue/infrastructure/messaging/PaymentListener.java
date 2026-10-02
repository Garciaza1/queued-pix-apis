package itau.persistence.queue.infrastructure.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import itau.persistence.queue.domain.port.in.PagamentoPersistenceUseCase;
import itau.persistence.queue.domain.model.PagamentoMessage;
import itau.pix.commons.messaging.RabbitMQConstants;

@Component
public class PaymentListener {

    private final PagamentoPersistenceUseCase persistenceService;

    public PaymentListener(PagamentoPersistenceUseCase persistenceService) {
        this.persistenceService = persistenceService;
    }

    @RabbitListener(queues = RabbitMQConstants.FILA_PAGAMENTO_SUCESSO)
    public void receiveSuccess(@Payload PagamentoMessage message) {
        System.out.println("📥 [SUCCESS QUEUE] Received message: " + message);
        persistenceService.persist(message, true);
    }

    @RabbitListener(queues = RabbitMQConstants.FILA_PAGAMENTO_FALHOU)
    public void receiveFailed(@Payload PagamentoMessage message) {
        System.out.println("📥 [FAILED QUEUE] Received message: " + message);
        persistenceService.persist(message, false);
    }

}
