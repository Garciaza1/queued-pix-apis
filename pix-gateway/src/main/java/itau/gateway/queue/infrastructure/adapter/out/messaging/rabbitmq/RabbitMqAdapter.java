package itau.gateway.queue.infrastructure.adapter.out.messaging.rabbitmq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import itau.gateway.queue.domain.model.pagamento.PagamentoMessage;
import itau.gateway.queue.domain.port.out.PaymentMessageSenderPort;
import itau.pix.commons.messaging.RabbitMQConstants;

@Component
public class RabbitMqAdapter implements PaymentMessageSenderPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void sendPaymentMessage(PagamentoMessage paymentMessage) {
        rabbitTemplate.convertAndSend(RabbitMQConstants.FILA_PAGAMENTO, paymentMessage);
    }
}
