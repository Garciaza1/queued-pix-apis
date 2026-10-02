package itau.worker.queue.infrastructure.adapter.out.persistence.mongo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import itau.worker.queue.infrastructure.adapter.out.persistence.mongo.document.ChavePixDocument;

@Repository
public interface SpringDataChavePixRepository extends MongoRepository<ChavePixDocument, String> {
    boolean existsByValorChave(String valorChave);
    List<ChavePixDocument> findByTipoChave(String tipoChave);
    List<ChavePixDocument> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta);
    Optional<ChavePixDocument> findByNumeroConta(String numeroConta);
    Optional<ChavePixDocument> findByValorChave(String valorChave);
}
