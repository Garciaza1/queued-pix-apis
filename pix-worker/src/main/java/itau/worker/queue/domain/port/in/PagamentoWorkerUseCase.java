package itau.worker.queue.domain.port.in;

import itau.worker.queue.domain.model.PagamentoMessage;

public interface PagamentoWorkerUseCase {
    void process(PagamentoMessage pagamentoMessage);
}
