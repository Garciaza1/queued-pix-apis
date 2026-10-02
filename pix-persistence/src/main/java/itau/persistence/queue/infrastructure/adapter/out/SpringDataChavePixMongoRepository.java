package itau.persistence.queue.infrastructure.adapter.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataChavePixMongoRepository extends MongoRepository<ChavePixDocument, UUID> {
    Optional<ChavePixDocument> findByValorChave(String valorChave);
    Optional<ChavePixDocument> findByNumeroConta(String numeroConta);
    Optional<ChavePixDocument> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta);
}
