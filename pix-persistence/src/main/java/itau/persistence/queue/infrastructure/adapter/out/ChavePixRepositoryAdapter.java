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
        return mongoRepository.findByValorChave(valorChave);
    }

    @Override
    public Optional<ChavePix> findByConta(String numeroAgencia, String numeroConta) {
        return mongoRepository.findByNumeroAgenciaAndNumeroConta(numeroAgencia, numeroConta);
    }

    @Override
    public Optional<ChavePix> findByNumeroConta(String numeroConta) {
        return mongoRepository.findByNumeroConta(numeroConta);
    }

    @Override
    public ChavePix save(ChavePix chavePix) {
        return mongoRepository.save(Objects.requireNonNull(chavePix, "ChavePix não pode ser nulo"));
    }

    @Override
    public Optional<ChavePix> findById(UUID id) {
        return mongoRepository.findById(Objects.requireNonNull(id, "UUID não pode ser nulo"));
    }
}
