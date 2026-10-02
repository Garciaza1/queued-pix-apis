package itau.persistence.queue.domain.port.out;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import itau.persistence.queue.domain.model.ChavePix;

public interface ChavePixRepositoryPort {

    Optional<ChavePix> findByValorChave(String valorChave);

    Optional<ChavePix> findByConta(String numeroAgencia, String numeroConta);

    Optional<ChavePix> findByNumeroConta(String numeroConta);

    Optional<ChavePix> findById(UUID id);

    /** Atômico: confere e debita no mesmo passo. Retorna false, sem alterar nada, se o saldo é insuficiente ou a chave não existe. */
    boolean debitarSeHouverSaldo(UUID chaveId, BigDecimal valor);

    /** Atômico. Retorna false, sem alterar nada, se a chave não existe. */
    boolean creditar(UUID chaveId, BigDecimal valor);
}
