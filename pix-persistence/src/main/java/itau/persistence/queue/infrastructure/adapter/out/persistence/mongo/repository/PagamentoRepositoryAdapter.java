package itau.persistence.queue.infrastructure.adapter.out.persistence.mongo.repository;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import itau.persistence.queue.domain.port.out.PagamentoRepositoryPort;
import itau.persistence.queue.domain.model.Pagamento;
import itau.persistence.queue.infrastructure.adapter.out.persistence.mongo.document.PagamentoDocument;

@Component
public class PagamentoRepositoryAdapter implements PagamentoRepositoryPort {

    private final SpringDataPagamentoRepository repository;

    public PagamentoRepositoryAdapter(SpringDataPagamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public Pagamento save(Pagamento pagamento) {
        var saved = repository.save(toDocument(Objects.requireNonNull(pagamento, "Pagamento não pode ser nulo")));
        return toDomain(saved);
    }

    @Override
    public Optional<Pagamento> findById(String id) {
        return repository.findById(Objects.requireNonNull(id, "ID não pode ser nulo")).map(this::toDomain);
    }

    private PagamentoDocument toDocument(Pagamento domain) {
        return new PagamentoDocument(
                domain.getId(),
                domain.getAmount(),
                domain.getSenderAccount(),
                domain.getReceiverPixKey(),
                domain.getStatus(),
                domain.getErrorDescription()
        );
    }

    private Pagamento toDomain(PagamentoDocument document) {
        return new Pagamento(
                document.getId(),
                document.getAmount(),
                document.getSenderAccount(),
                document.getReceiverPixKey(),
                document.getStatus(),
                document.getErrorDescription()
        );
    }
}
