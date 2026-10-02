package itau.worker.queue.infrastructure.adapter.in.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import itau.worker.queue.domain.port.in.PagamentoWorkerUseCase;
import itau.worker.queue.domain.model.PagamentoMessage;

@Component
public class PaymentListener {

    private final PagamentoWorkerUseCase workerService;

    public PaymentListener(PagamentoWorkerUseCase workerService) {
        this.workerService = workerService;
    }

    @RabbitListener(queues = "#{paymentQueue.name}") // usa o nome da fila do bean (RabbitMQConfig) 
    public void receive(PagamentoMessage message) {
        workerService.process(message);
    }
}
