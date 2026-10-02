package itau.gateway.queue.domain.port.out;

import itau.gateway.queue.domain.model.pagamento.Pagamento;

public interface PaymentRepositoryPort {
    void save(Pagamento pagamento);
}
