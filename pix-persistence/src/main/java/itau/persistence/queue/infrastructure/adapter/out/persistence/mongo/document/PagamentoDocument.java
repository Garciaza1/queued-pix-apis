package itau.persistence.queue.infrastructure.adapter.out.persistence.mongo.document;

import java.math.BigDecimal;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import itau.pix.commons.config.DatabaseConstants;
import itau.pix.commons.enums.StatusPagamento;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Document(collection = DatabaseConstants.PAGAMENTO_COLLECTION)
public class PagamentoDocument {

    @Id
    private String id;
    private BigDecimal amount;
    private String senderAccount;
    private String receiverPixKey;
    private StatusPagamento status;
    private String errorDescription;
}
