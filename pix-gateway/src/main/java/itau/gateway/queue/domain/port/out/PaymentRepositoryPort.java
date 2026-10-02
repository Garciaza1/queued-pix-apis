package itau.gateway.queue.domain.port.out;

import java.util.Optional;
import itau.gateway.queue.domain.model.pagamento.Pagamento;

public interface PaymentRepositoryPort {
    void save(Pagamento pagamento);
    Optional<Pagamento> findById(String id);
}
