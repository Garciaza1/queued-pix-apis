package itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.repository;

import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import itau.gateway.queue.domain.model.pagamento.IdempotencyRecord;
import itau.gateway.queue.domain.port.out.IdempotencyRepositoryPort;
import itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.document.IdempotencyRecordDocument;

@Component
public class IdempotencyRepositoryAdapter implements IdempotencyRepositoryPort {

    private final MongoTemplate mongoTemplate;

    public IdempotencyRepositoryAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<IdempotencyRecord> registrarSeNova(IdempotencyRecord record) {
        try {
            mongoTemplate.insert(toDocument(record));
            return Optional.empty();
        } catch (DuplicateKeyException chaveJaExiste) {
            IdempotencyRecordDocument existente = mongoTemplate.findById(record.key(), IdempotencyRecordDocument.class);
            if (existente == null) {
                throw new IllegalStateException("Idempotency-Key liberada durante o registro; repita a requisição.", chaveJaExiste);
            }
            return Optional.of(toDomain(existente));
        }
    }

    @Override
    public void liberar(String key) {
        mongoTemplate.remove(Query.query(Criteria.where("_id").is(key)), IdempotencyRecordDocument.class);
    }

    private IdempotencyRecordDocument toDocument(IdempotencyRecord record) {
        return new IdempotencyRecordDocument(record.key(), record.fingerprint(), record.paymentId(), record.createdAt());
    }

    private IdempotencyRecord toDomain(IdempotencyRecordDocument document) {
        return new IdempotencyRecord(document.getKey(), document.getFingerprint(), document.getPaymentId(), document.getCreatedAt());
    }
}
