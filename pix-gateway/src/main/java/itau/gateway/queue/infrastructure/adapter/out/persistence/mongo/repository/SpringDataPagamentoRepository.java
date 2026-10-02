package itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import itau.gateway.queue.infrastructure.adapter.out.persistence.mongo.document.PagamentoDocument;

@Repository
public interface SpringDataPagamentoRepository extends MongoRepository<PagamentoDocument, String> {

}
