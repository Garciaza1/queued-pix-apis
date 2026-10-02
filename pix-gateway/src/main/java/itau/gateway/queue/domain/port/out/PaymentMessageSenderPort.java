package itau.gateway.queue.domain.port.out;

import itau.gateway.queue.domain.model.pagamento.PagamentoMessage;

public interface PaymentMessageSenderPort {
    void sendPaymentMessage(PagamentoMessage paymentMessage);
}
