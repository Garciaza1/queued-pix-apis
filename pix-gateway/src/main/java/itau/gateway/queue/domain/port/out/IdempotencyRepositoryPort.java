package itau.gateway.queue.domain.port.out;

import java.util.Optional;

import itau.gateway.queue.domain.model.pagamento.IdempotencyRecord;

public interface IdempotencyRepositoryPort {

    /** Registra a chave de forma atômica: vazio se registrou agora; se a chave já existia, devolve o registro anterior. */
    Optional<IdempotencyRecord> registrarSeNova(IdempotencyRecord record);

    void liberar(String key);
}
