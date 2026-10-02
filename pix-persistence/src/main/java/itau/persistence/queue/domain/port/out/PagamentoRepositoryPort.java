package itau.persistence.queue.domain.port.out;

import java.util.Optional;

import itau.persistence.queue.domain.model.Pagamento;

public interface PagamentoRepositoryPort {
    Pagamento save(Pagamento pagamento);
    Optional<Pagamento> findById(String id);
}
