package itau.gateway.queue.infrastructure.adapter.out;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Repository;

import itau.gateway.queue.domain.model.chave.ChavePix;
import itau.gateway.queue.domain.port.out.ChavePixRepositoryPort;

@Repository
public class ChavePixRepositoryAdapter implements ChavePixRepositoryPort {

    private final SpringDataChavePixRepository repository;

    public ChavePixRepositoryAdapter(SpringDataChavePixRepository repository) {
        this.repository = repository;
    }

    @Override
    public boolean existsByValorChave(String valorChave) {
        return repository.existsByValorChave(valorChave);
    }

    @Override
    public ChavePix save(ChavePix chavePix) {
        var saved = repository.save(toDocument(Objects.requireNonNull(chavePix, "ChavePix não pode ser nulo")));
        return toDomain(saved);
    }

    @Override
    public Optional<ChavePix> findById(UUID id) {
        return repository.findById(Objects.requireNonNull(id, "UUID não pode ser nulo")).map(this::toDomain);
    }

    @Override
    public List<ChavePix> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public List<ChavePix> findByTipoChave(String tipoChave) {
        return repository.findByTipoChave(tipoChave).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ChavePix> findByNumeroAgenciaAndNumeroConta(String numeroAgencia, String numeroConta) {
        return repository.findByNumeroAgenciaAndNumeroConta(numeroAgencia, numeroConta).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ChavePix> findByNomeCorrentista(String nomeCorrentista) {
        return repository.findByNomeCorrentista(nomeCorrentista).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ChavePix> findByDataHoraInclusaoBetween(java.time.LocalDateTime inicio, java.time.LocalDateTime fim) {
        return repository.findByDataHoraInclusaoBetween(inicio, fim).stream().map(this::toDomain).toList();
    }

    @Override
    public List<ChavePix> findByDataHoraInativacaoBetween(java.time.LocalDateTime inicio, java.time.LocalDateTime fim) {
        return repository.findByDataHoraInativacaoBetween(inicio, fim).stream().map(this::toDomain).toList();
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
