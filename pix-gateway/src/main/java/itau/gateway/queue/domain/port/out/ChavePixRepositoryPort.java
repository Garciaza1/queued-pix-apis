package itau.gateway.queue.domain.port.out;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import itau.gateway.queue.domain.model.chave.ChavePix;

public interface ChavePixRepositoryPort {
    ChavePix save(ChavePix chavePix);
    Optional<ChavePix> findById(UUID id);
    boolean existsByValorChave(String valorChave);
    List<ChavePix> findAll();
    List<ChavePix> findByTipoChave(String tipoChave);
    List<ChavePix> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta);
    List<ChavePix> findByNomeCorrentista(String nomeCorrentista);
    List<ChavePix> findByDataHoraInclusaoBetween(LocalDateTime inicio, LocalDateTime fim);
    List<ChavePix> findByDataHoraInativacaoBetween(LocalDateTime inicio, LocalDateTime fim);
}
