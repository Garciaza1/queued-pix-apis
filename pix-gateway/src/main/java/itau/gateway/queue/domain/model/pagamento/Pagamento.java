package itau.gateway.queue.domain.model.pagamento;

import java.math.BigDecimal;

import itau.pix.commons.enums.StatusPagamento;
import lombok.Data;

@Data
public class Pagamento {

    private String id;
    private BigDecimal amount;
    private String senderAccount;
    private String receiverPixKey;
    private StatusPagamento status;
    private int retryCount = 0;
}
