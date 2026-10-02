package itau.persistence.queue.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import itau.persistence.queue.domain.model.ChavePix;

@Repository
public interface ChavePixRepositoryPort {

    Optional<ChavePix> findByValorChave(String valorChave);

    Optional<ChavePix> findByConta(String numeroAgencia, String numeroConta);

    Optional<ChavePix> findByNumeroConta(String numeroConta);

    ChavePix save(ChavePix chavePix);

    Optional<ChavePix> findById(UUID id);
}
