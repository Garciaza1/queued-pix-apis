package itau.worker.queue.application.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Service;

import itau.pix.commons.enums.StatusPagamento;
import itau.worker.queue.domain.port.in.PagamentoWorkerUseCase;
import itau.worker.queue.application.port.validator.PagamentoValidator;
import itau.worker.queue.domain.model.ChavePix;
import itau.worker.queue.domain.model.PagamentoMessage;
import itau.worker.queue.domain.port.out.ChavePixRepositoryPort;
import itau.worker.queue.domain.port.out.PagamentoResultPublisherPort;

@Service
public class PagamentoWorkerUseCaseImpl implements PagamentoWorkerUseCase {

    private final PagamentoResultPublisherPort resultPublisher;
    private final ChavePixRepositoryPort chavePixRepository;
    private final PagamentoValidator validator;

    public PagamentoWorkerUseCaseImpl(
            PagamentoResultPublisherPort resultPublisher,
            ChavePixRepositoryPort chavePixRepository,
            PagamentoValidator validator
    ) {
        this.resultPublisher = resultPublisher;
        this.chavePixRepository = chavePixRepository;
        this.validator = validator;
    }

    @Override
    public void process(PagamentoMessage message) {
        if (message == null) {
            System.out.println("Mensagem nula recebida pelo worker.");
            return;
        }

        System.out.println("Processing payment: " + message.getId());

        Optional<ChavePix> senderOpt = chavePixRepository.findByNumeroConta(message.getSenderAccount());
        Optional<ChavePix> receiverOpt = chavePixRepository.findByValorChave(message.getReceiverPixKey());

        // usar validator para todas as regras
        var result = validator.validate(message, senderOpt, receiverOpt);
        if (!result.isValid()) {
            message.setErrorDescription(result.getError());
            handleFailed(message);
            return;
        }

        // se chegou aqui, validações ok -> enviar sucesso
        handleSuccess(message);
    }

    private void handleFailed(PagamentoMessage message) {
        Objects.requireNonNull(message.getId(), "ID da mensagem não pode ser nulo");
        if (message.getRetryCount() < 3) {
            message.setRetryCount(message.getRetryCount() + 1);
            // ao reenfileirar, manter status null (indica pendente) e enviar para fila principal
            message.setStatus(null);
            resultPublisher.publishRetry(message);
            System.out.println("🔄 Retrying payment " + message.getId() + " attempt " + message.getRetryCount() + " - reason: " + message.getErrorDescription());
        } else {
            message.setStatus(StatusPagamento.FALHOU);
            resultPublisher.publishFailure(message);
            System.out.println("❌ Payment " + message.getId() + " failed after 3 attempts: " + message.getErrorDescription());
        }
    }

    private void handleSuccess(PagamentoMessage message) {
        Objects.requireNonNull(message.getId(), "ID da mensagem não pode ser nulo");
        message.setStatus(StatusPagamento.SUCESSO);
        resultPublisher.publishSuccess(message);
        System.out.println("✅ Payment " + message.getId() + " validated successfully");
    }
}
