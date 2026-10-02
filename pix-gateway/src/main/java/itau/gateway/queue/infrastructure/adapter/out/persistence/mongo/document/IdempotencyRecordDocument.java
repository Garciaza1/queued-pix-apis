package itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.document;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "idempotency_keys")
public class IdempotencyRecordDocument {

    @Id
    private String key;
    private String fingerprint;
    private String paymentId;

    @Indexed(expireAfter = "24h")
    private Instant createdAt;
}
