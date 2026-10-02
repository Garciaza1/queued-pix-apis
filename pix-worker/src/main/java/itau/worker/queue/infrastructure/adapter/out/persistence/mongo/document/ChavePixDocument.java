package itau.worker.queue.infrastructure.adapter.out.persistence.mongo.document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import itau.pix.commons.config.DatabaseConstants;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = DatabaseConstants.PIX_COLLECTION)
public class ChavePixDocument {

    @Id
    private String id;
    private String tipoChave;
    private String valorChave;
    private String tipoConta;
    private String numeroAgencia;
    private String numeroConta;
    private String nomeCorrentista;
    private String sobrenomeCorrentista;
    private BigDecimal saldo;
    private LocalDateTime dataHoraInclusao;
    private LocalDateTime dataHoraInativacao;
    private String status;
}
