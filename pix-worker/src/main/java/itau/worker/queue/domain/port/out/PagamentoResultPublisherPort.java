package itau.worker.queue.domain.port.out;

import itau.worker.queue.domain.model.PagamentoMessage;

public interface PagamentoResultPublisherPort {
    void publishRetry(PagamentoMessage message);
    void publishFailure(PagamentoMessage message);
    void publishSuccess(PagamentoMessage message);
}
