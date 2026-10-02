package itau.persistence.queue.domain.port.out;

import java.util.Optional;
import java.util.UUID;

import itau.persistence.queue.domain.model.ChavePix;

public interface ChavePixRepositoryPort {

    Optional<ChavePix> findByValorChave(String valorChave);

    Optional<ChavePix> findByConta(String numeroAgencia, String numeroConta);

    Optional<ChavePix> findByNumeroConta(String numeroConta);

    ChavePix save(ChavePix chavePix);

    Optional<ChavePix> findById(UUID id);
}
