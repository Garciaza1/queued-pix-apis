package itau.worker.queue.infrastructure.adapter.out.messaging.rabbitmq;

import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import itau.pix.commons.messaging.RabbitMQConstants;
import itau.worker.queue.domain.model.PagamentoMessage;
import itau.worker.queue.domain.port.out.PagamentoResultPublisherPort;

@Component
public class RabbitMqAdapter implements PagamentoResultPublisherPort {

    private final RabbitTemplate rabbitTemplate;

    public RabbitMqAdapter(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Override
    public void publishRetry(PagamentoMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConstants.FILA_PAGAMENTO, message, new CorrelationData(message.getId()));
    }

    @Override
    public void publishFailure(PagamentoMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConstants.FILA_PAGAMENTO_FALHOU, message, new CorrelationData(message.getId()));
    }

    @Override
    public void publishSuccess(PagamentoMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConstants.FILA_PAGAMENTO_SUCESSO, message, new CorrelationData(message.getId()));
    }
}
