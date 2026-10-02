package itau.persistence.queue.infrastructure.adapter.out.persistence.mongo.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import itau.persistence.queue.infrastructure.adapter.out.persistence.mongo.document.ChavePixDocument;

@Repository
public interface SpringDataChavePixMongoRepository extends MongoRepository<ChavePixDocument, UUID> {
    Optional<ChavePixDocument> findByValorChave(String valorChave);
    Optional<ChavePixDocument> findByNumeroConta(String numeroConta);
    Optional<ChavePixDocument> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta);
}
