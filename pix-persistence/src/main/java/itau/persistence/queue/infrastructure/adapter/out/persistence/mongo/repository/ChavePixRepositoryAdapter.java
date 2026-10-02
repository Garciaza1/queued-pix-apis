package itau.persistence.queue.infrastructure.adapter.out.persistence.mongo.repository;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Component;

import itau.persistence.queue.domain.port.out.ChavePixRepositoryPort;
import itau.persistence.queue.domain.model.ChavePix;
import itau.persistence.queue.infrastructure.adapter.out.persistence.mongo.document.ChavePixDocument;

@Component
public class ChavePixRepositoryAdapter implements ChavePixRepositoryPort {

    private final SpringDataChavePixMongoRepository mongoRepository;
    private final MongoTemplate mongoTemplate;

    public ChavePixRepositoryAdapter(SpringDataChavePixMongoRepository mongoRepository, MongoTemplate mongoTemplate) {
        this.mongoRepository = mongoRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public Optional<ChavePix> findByValorChave(String valorChave) {
        return mongoRepository.findByValorChave(valorChave).map(this::toDomain);
    }

    @Override
    public Optional<ChavePix> findByConta(String numeroAgencia, String numeroConta) {
        return mongoRepository.findByNumeroAgenciaAndNumeroConta(numeroAgencia, numeroConta).map(this::toDomain);
    }

    @Override
    public Optional<ChavePix> findByNumeroConta(String numeroConta) {
        return mongoRepository.findByNumeroConta(numeroConta).map(this::toDomain);
    }

    @Override
    public Optional<ChavePix> findById(UUID id) {
        return mongoRepository.findById(Objects.requireNonNull(id, "UUID não pode ser nulo")).map(this::toDomain);
    }

    @Override
    public boolean debitarSeHouverSaldo(UUID chaveId, BigDecimal valor) {
        Query query = Query.query(Criteria.where("id").is(chaveId).and("saldo").gte(valor));
        Update update = new Update().inc("saldo", valor.negate());
        return mongoTemplate.updateFirst(query, update, ChavePixDocument.class).getMatchedCount() == 1;
    }

    @Override
    public boolean creditar(UUID chaveId, BigDecimal valor) {
        Query query = Query.query(Criteria.where("id").is(chaveId));
        Update update = new Update().inc("saldo", valor);
        return mongoTemplate.updateFirst(query, update, ChavePixDocument.class).getMatchedCount() == 1;
    }

    private ChavePix toDomain(ChavePixDocument document) {
        return ChavePix.builder()
                .id(document.getId())
                .tipoChave(document.getTipoChave())
                .valorChave(document.getValorChave())
                .tipoConta(document.getTipoConta())
                .numeroAgencia(document.getNumeroAgencia())
                .numeroConta(document.getNumeroConta())
                .nomeCorrentista(document.getNomeCorrentista())
                .sobrenomeCorrentista(document.getSobrenomeCorrentista())
                .saldo(document.getSaldo())
                .dataHoraInclusao(document.getDataHoraInclusao())
                .dataHoraInativacao(document.getDataHoraInativacao())
                .status(document.getStatus())
                .build();
    }
}
