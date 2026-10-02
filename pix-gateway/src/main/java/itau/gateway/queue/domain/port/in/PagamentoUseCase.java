package itau.gateway.queue.domain.port.in;

import itau.gateway.queue.domain.model.pagamento.Pagamento;

public interface PagamentoUseCase {

    /** Aceita o pagamento e devolve seu id. A mesma chave de idempotência sempre devolve o mesmo id, sem reprocessar. */
    String processPayment(Pagamento pagamento, String idempotencyKey);
}
