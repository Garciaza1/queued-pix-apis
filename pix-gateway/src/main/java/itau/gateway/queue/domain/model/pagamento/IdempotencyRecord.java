package itau.gateway.queue.domain.model.pagamento;

import java.time.Instant;

public record IdempotencyRecord(String key, String fingerprint, String paymentId, Instant createdAt) {
}
