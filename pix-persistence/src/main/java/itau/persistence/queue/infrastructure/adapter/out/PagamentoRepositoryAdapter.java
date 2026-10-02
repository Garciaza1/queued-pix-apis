package itau.persistence.queue.infrastructure.adapter.out;

import java.util.Objects;

import org.springframework.stereotype.Component;

import itau.persistence.queue.domain.port.out.PagamentoRepositoryPort;
import itau.persistence.queue.domain.model.Pagamento;

@Component
public class PagamentoRepositoryAdapter implements PagamentoRepositoryPort {

    private final SpringDataPagamentoRepository repository;

    public PagamentoRepositoryAdapter(SpringDataPagamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pagamento save(Pagamento pagamento) {
        return repository.save(Objects.requireNonNull(pagamento, "Pagamento não pode ser nulo"));
    }

    @Override
    public java.util.Optional<Pagamento> findById(String id) {
        return repository.findById(Objects.requireNonNull(id, "ID não pode ser nulo"));
    }
}
