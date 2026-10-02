package itau.persistence.queue.domain.model;

import java.math.BigDecimal;

import itau.pix.commons.enums.StatusPagamento;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Pagamento {

    private String id;
    private BigDecimal amount;
    private String senderAccount;
    private String receiverPixKey;
    private StatusPagamento status;
    private String errorDescription;

    public boolean isFinalizado() {
        return status != null && status.isTerminal();
    }
}
