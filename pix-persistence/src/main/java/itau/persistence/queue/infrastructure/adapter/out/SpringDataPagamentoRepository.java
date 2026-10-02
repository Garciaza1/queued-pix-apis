package itau.persistence.queue.infrastructure.adapter.out;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataPagamentoRepository extends MongoRepository<PagamentoDocument, String> {

}
