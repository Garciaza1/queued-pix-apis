package itau.gateway.queue.application.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import itau.gateway.queue.domain.exception.NotFoundException;
import itau.gateway.queue.domain.exception.UnprocessableEntityException;
import itau.gateway.queue.domain.model.chave.ChavePix;
import itau.gateway.queue.domain.model.chave.ConsultaChavePixResponse;
import itau.gateway.queue.domain.model.chave.InclusaoChavePixRequest;
import itau.gateway.queue.domain.model.chave.InclusaoChavePixResponse;
import itau.gateway.queue.domain.port.in.ChavePixUseCase;
import itau.gateway.queue.domain.port.out.ChavePixRepositoryPort;

@Service
public class ChavePixUseCaseImpl implements ChavePixUseCase {

    private final ChavePixRepositoryPort repository;

    public ChavePixUseCaseImpl(ChavePixRepositoryPort repository) {
        this.repository = repository;
    }

    // inclusão de chave pix
    @Override
    public InclusaoChavePixResponse cadastrar(InclusaoChavePixRequest request) {

        var chave = ChavePix.builder()
                .id(UUID.randomUUID())
                .tipoChave(request.getTipoChave())
                .valorChave(request.getValorChave())
                .saldo(request.getSaldo())
                .tipoConta(request.getTipoConta())
                .numeroAgencia(request.getNumeroAgencia())
                .numeroConta(request.getNumeroConta())
                .nomeCorrentista(request.getNomeCorrentista())
                .sobrenomeCorrentista(request.getSobrenomeCorrentista())
                .dataHoraInclusao(LocalDateTime.now())
                .build();

        var saved = repository.save(chave);

        return new InclusaoChavePixResponse(
                saved.getId(),
                saved.getTipoChave(),
                saved.getValorChave(),
                saved.getTipoConta(),
                saved.getNumeroAgencia(),
                saved.getNumeroConta(),
                saved.getSaldo(),
                saved.getNomeCorrentista(),
                saved.getSobrenomeCorrentista(),
                saved.getDataHoraInclusao(),
                saved.getDataHoraInativacao()
        );
    }

    // Consulta da chave pix
    @Override
    public List<ConsultaChavePixResponse> consultar(
            UUID id,
            String tipoChave,
            String numeroAgencia,
            String numeroConta,
            String nomeCorrentista,
            LocalDate dataInclusao,
            LocalDate dataInativacao) {

        validarRegrasDeNegocio(id, tipoChave, numeroAgencia, numeroConta, nomeCorrentista, dataInclusao, dataInativacao);

        List<ChavePix> resultados = (id != null)
                ? buscarPorId(id)
                : buscarPorFiltros(tipoChave, numeroAgencia, numeroConta, nomeCorrentista, dataInclusao, dataInativacao);

        if (resultados.isEmpty()) {
            throw new NotFoundException("Nenhuma chave Pix encontrada.");
        }

        return resultados.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // funções a parte
    private ConsultaChavePixResponse toResponse(ChavePix chave) {
        ConsultaChavePixResponse resp = new ConsultaChavePixResponse();
        resp.setId(chave.getId());
        resp.setTipoChave(chave.getTipoChave());
        resp.setValorChave(chave.getValorChave());
        resp.setTipoConta(chave.getTipoConta());
        resp.setNumeroAgencia(chave.getNumeroAgencia());
        resp.setNumeroConta(chave.getNumeroConta());
        resp.setNomeCorrentista(Optional.ofNullable(chave.getNomeCorrentista()).orElse(""));
        resp.setSobrenomeCorrentista(Optional.ofNullable(chave.getSobrenomeCorrentista()).orElse(""));
        resp.setDataHoraInclusao(chave.getDataHoraInclusao());
        resp.setDataHoraInativacao(chave.getDataHoraInativacao());
        return resp;
    }

    // validação do filtro
    private void validarRegrasDeNegocio(UUID id,
            String tipoChave,
            String numeroAgencia,
            String numeroConta,
            String nomeCorrentista,
            LocalDate dataInclusao,
            LocalDate dataInativacao) {
        if (id != null && (tipoChave != null || numeroAgencia != null || numeroConta != null
                || nomeCorrentista != null || dataInclusao != null || dataInativacao != null)) {
            throw new UnprocessableEntityException("Se o ID for informado, nenhum outro filtro pode ser usado.");
        }

        if (dataInclusao != null && dataInativacao != null) {
            throw new UnprocessableEntityException("Não é permitido informar data de inclusão e data de inativação juntas.");
        }
    }

    // por ID
    private List<ChavePix> buscarPorId(UUID id) {
        return List.of(repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chave Pix não encontrada.")));
    }

    // por Filtros e verifica se estão nulos
    private List<ChavePix> buscarPorFiltros(String tipoChave,
            String numeroAgencia,
            String numeroConta,
            String nomeCorrentista,
            LocalDate dataInclusao,
            LocalDate dataInativacao) {
        if (todosNulos(tipoChave, numeroAgencia, numeroConta, nomeCorrentista, dataInclusao, dataInativacao)) {
            return repository.findAll();
        }

        List<List<ChavePix>> consultas = List.of(
                Optional.ofNullable(tipoChave)
                        .map(repository::findByTipoChave)
                        .orElse(List.of()),
                (numeroAgencia != null && numeroConta != null)
                        ? repository.findByNumeroAgenciaAndNumeroConta(numeroAgencia, numeroConta)
                        : List.of(),
                Optional.ofNullable(nomeCorrentista)
                        .map(repository::findByNomeCorrentista)
                        .orElse(List.of()),
                Optional.ofNullable(dataInclusao)
                        .map(d -> repository.findByDataHoraInclusaoBetween(d.atStartOfDay(), d.plusDays(1).atStartOfDay().minusSeconds(1)))
                        .orElse(List.of()),
                Optional.ofNullable(dataInativacao)
                        .map(d -> repository.findByDataHoraInativacaoBetween(d.atStartOfDay(), d.plusDays(1).atStartOfDay().minusSeconds(1)))
                        .orElse(List.of())
        );

        return consultas.stream()
                .flatMap(List::stream)
                .toList();
    }

    // verifica se todos os valores são nulos
    private boolean todosNulos(Object... values) {
        return Arrays.stream(values).allMatch(Objects::isNull);
    }

}
