package itau.persistence.queue.domain.port.out;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import itau.persistence.queue.domain.model.Pagamento;

@Repository
public interface PagamentoRepositoryPort {
    Pagamento save(Pagamento pagamento);
    Optional<Pagamento> findById(String id);
}
