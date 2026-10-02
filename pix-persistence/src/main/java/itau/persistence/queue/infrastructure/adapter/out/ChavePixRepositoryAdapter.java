package itau.persistence.queue.infrastructure.adapter.out;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import itau.persistence.queue.domain.port.out.ChavePixRepositoryPort;
import itau.persistence.queue.domain.model.ChavePix;

@Component
public class ChavePixRepositoryAdapter implements ChavePixRepositoryPort {

    private final SpringDataChavePixMongoRepository mongoRepository;

    public ChavePixRepositoryAdapter(SpringDataChavePixMongoRepository mongoRepository) {
        this.mongoRepository = mongoRepository;
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
    public ChavePix save(ChavePix chavePix) {
        var saved = mongoRepository.save(toDocument(Objects.requireNonNull(chavePix, "ChavePix não pode ser nulo")));
        return toDomain(saved);
    }

    @Override
    public Optional<ChavePix> findById(UUID id) {
        return mongoRepository.findById(Objects.requireNonNull(id, "UUID não pode ser nulo")).map(this::toDomain);
    }

    private ChavePixDocument toDocument(ChavePix domain) {
        return ChavePixDocument.builder()
                .id(domain.getId())
                .tipoChave(domain.getTipoChave())
                .valorChave(domain.getValorChave())
                .tipoConta(domain.getTipoConta())
                .numeroAgencia(domain.getNumeroAgencia())
                .numeroConta(domain.getNumeroConta())
                .nomeCorrentista(domain.getNomeCorrentista())
                .sobrenomeCorrentista(domain.getSobrenomeCorrentista())
                .saldo(domain.getSaldo())
                .dataHoraInclusao(domain.getDataHoraInclusao())
                .dataHoraInativacao(domain.getDataHoraInativacao())
                .status(domain.getStatus())
                .build();
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
