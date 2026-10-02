package itau.gateway.queue.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import itau.gateway.queue.domain.model.pagamento.Pagamento;
import itau.gateway.queue.domain.model.pagamento.PagamentoRequest;
import itau.gateway.queue.domain.port.in.PagamentoUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("api/pix/payments")
public class PagamentoController {

    private final PagamentoUseCase pagamentoUseCase;

    public PagamentoController(PagamentoUseCase pagamentoUseCase) {
        this.pagamentoUseCase = pagamentoUseCase;
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> processPayment(
            @RequestHeader("Idempotency-Key") @NotBlank @Size(max = 64) String idempotencyKey,
            @Valid @RequestBody PagamentoRequest paymentRequest) {
        Pagamento pagamento = convertToPagamento(paymentRequest);
        String paymentId = pagamentoUseCase.processPayment(pagamento, idempotencyKey);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new PagamentoResponse(paymentId));
    }

    private Pagamento convertToPagamento(PagamentoRequest paymentRequest) {
        Pagamento pagamento = new Pagamento();
        pagamento.setAmount(paymentRequest.getAmount());
        pagamento.setSenderAccount(paymentRequest.getSenderAccount());
        pagamento.setReceiverPixKey(paymentRequest.getReceiverPixKey());
        return pagamento;
    }

}
