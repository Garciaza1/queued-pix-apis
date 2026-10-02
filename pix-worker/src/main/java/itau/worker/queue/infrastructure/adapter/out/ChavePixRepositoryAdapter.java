package itau.worker.queue.infrastructure.adapter.out;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Repository;

import itau.worker.queue.domain.model.ChavePix;
import itau.worker.queue.domain.port.out.ChavePixRepositoryPort;

@Repository
public class ChavePixRepositoryAdapter implements ChavePixRepositoryPort {

    private final SpringDataChavePixRepository repository;

    public ChavePixRepositoryAdapter(SpringDataChavePixRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<ChavePix> findById(String id) {
        return repository.findById(Objects.requireNonNull(id, "ID não pode ser nulo")).map(this::toDomain);
    }

    @Override
    public boolean existsByValorChave(String valorChave) {
        return repository.existsByValorChave(valorChave);
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
    public Optional<ChavePix> findByNumeroConta(String numeroConta) {
        return repository.findByNumeroConta(numeroConta).map(this::toDomain);
    }

    @Override
    public Optional<ChavePix> findByValorChave(String valorChave) {
        return repository.findByValorChave(valorChave).map(this::toDomain);
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
