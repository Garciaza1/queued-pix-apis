package itau.gateway.queue.infrastructure.adapter.out;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import itau.gateway.queue.domain.model.pagamento.Pagamento;
import itau.gateway.queue.domain.port.out.PaymentRepositoryPort;

@Repository
public class PaymentRepositoryAdapter implements PaymentRepositoryPort {

    private final SpringDataPagamentoRepository repository;

    public PaymentRepositoryAdapter(SpringDataPagamentoRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(Pagamento pagamento) {
        repository.save(toDocument(Objects.requireNonNull(pagamento, "Pagamento não pode ser nulo")));
    }

    @Override
    public Optional<Pagamento> findById(String id) {
        return repository.findById(Objects.requireNonNull(id, "ID não pode ser nulo")).map(this::toDomain);
    }

    private PagamentoDocument toDocument(Pagamento domain) {
        PagamentoDocument document = new PagamentoDocument();
        document.setId(domain.getId());
        document.setAmount(domain.getAmount());
        document.setSenderAccount(domain.getSenderAccount());
        document.setReceiverPixKey(domain.getReceiverPixKey());
        document.setStatus(domain.getStatus());
        document.setRetryCount(domain.getRetryCount());
        return document;
    }

    private Pagamento toDomain(PagamentoDocument document) {
        Pagamento domain = new Pagamento();
        domain.setId(document.getId());
        domain.setAmount(document.getAmount());
        domain.setSenderAccount(document.getSenderAccount());
        domain.setReceiverPixKey(document.getReceiverPixKey());
        domain.setStatus(document.getStatus());
        domain.setRetryCount(document.getRetryCount());
        return domain;
    }
}
