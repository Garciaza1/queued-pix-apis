package itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.document.ChavePixDocument;

@Repository
public interface SpringDataChavePixRepository extends MongoRepository<ChavePixDocument, UUID> {

    boolean existsByValorChave(String valorChave);

    List<ChavePixDocument> findByTipoChave(String tipoChave);

    List<ChavePixDocument> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta);

    List<ChavePixDocument> findByNomeCorrentista(String nomeCorrentista);

    List<ChavePixDocument> findByDataHoraInclusaoBetween(java.time.LocalDateTime inicio, java.time.LocalDateTime fim);

    List<ChavePixDocument> findByDataHoraInativacaoBetween(java.time.LocalDateTime inicio, java.time.LocalDateTime fim);
}
