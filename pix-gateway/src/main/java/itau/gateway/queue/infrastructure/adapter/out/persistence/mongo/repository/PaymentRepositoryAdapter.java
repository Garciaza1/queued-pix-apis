package itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.repository;

import java.util.Objects;

import org.springframework.stereotype.Repository;

import itau.gateway.queue.domain.model.pagamento.Pagamento;
import itau.gateway.queue.domain.port.out.PaymentRepositoryPort;
import itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.document.PagamentoDocument;

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
}
