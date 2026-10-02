package itau.gateway.queue.application.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;

import org.springframework.stereotype.Service;

import itau.gateway.queue.domain.exception.UnprocessableEntityException;
import itau.gateway.queue.domain.model.pagamento.IdempotencyRecord;
import itau.gateway.queue.domain.model.pagamento.Pagamento;
import itau.gateway.queue.domain.model.pagamento.PagamentoMessage;
import itau.gateway.queue.domain.port.in.PagamentoUseCase;
import itau.gateway.queue.domain.port.out.IdGeneratorPort;
import itau.gateway.queue.domain.port.out.IdempotencyRepositoryPort;
import itau.gateway.queue.domain.port.out.PaymentMessageSenderPort;
import itau.gateway.queue.domain.port.out.PaymentRepositoryPort;
import itau.pix.commons.enums.StatusPagamento;

@Service
public class PagamentoUseCaseImpl implements PagamentoUseCase {

    private final IdGeneratorPort idGenerator;
    private final PaymentRepositoryPort paymentRepository;
    private final IdempotencyRepositoryPort idempotencyRepository;
    private final PaymentMessageSenderPort messageSenderPort;

    public PagamentoUseCaseImpl(IdGeneratorPort idGenerator,
            PaymentRepositoryPort paymentRepository,
            IdempotencyRepositoryPort idempotencyRepository,
            PaymentMessageSenderPort messageSenderPort) {
        this.idGenerator = idGenerator;
        this.paymentRepository = paymentRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.messageSenderPort = messageSenderPort;
    }

    @Override
    public String processPayment(Pagamento paymentRequest, String idempotencyKey) {
        String fingerprint = fingerprintOf(paymentRequest);
        IdempotencyRecord novoRegistro = new IdempotencyRecord(idempotencyKey, fingerprint, idGenerator.generateId(), Instant.now());

        // A chave é reservada primeiro, de forma atômica: só uma requisição com essa chave segue adiante.
        Optional<IdempotencyRecord> registroAnterior = idempotencyRepository.registrarSeNova(novoRegistro);
        if (registroAnterior.isPresent()) {
            return reaproveitar(registroAnterior.get(), fingerprint);
        }

        String paymentId = novoRegistro.paymentId();

        Pagamento paymentEntity = new Pagamento();
        paymentEntity.setId(paymentId);
        paymentEntity.setAmount(paymentRequest.getAmount());
        paymentEntity.setSenderAccount(paymentRequest.getSenderAccount());
        paymentEntity.setReceiverPixKey(paymentRequest.getReceiverPixKey());
        paymentEntity.setStatus(StatusPagamento.PROCESSANDO);

        PagamentoMessage paymentMessage = new PagamentoMessage();
        paymentMessage.setId(paymentId);
        paymentMessage.setAmount(paymentRequest.getAmount());
        paymentMessage.setSenderAccount(paymentRequest.getSenderAccount());
        paymentMessage.setReceiverPixKey(paymentRequest.getReceiverPixKey());

        try {
            // O pagamento é gravado antes de publicar: o persistence pode terminar antes do retorno desta chamada.
            paymentRepository.save(paymentEntity);
            messageSenderPort.sendPaymentMessage(paymentMessage);
        } catch (RuntimeException falha) {
            desfazerAceite(idempotencyKey, paymentEntity, falha);
            throw falha;
        }

        System.out.println("Payment with ID " + paymentId + " sent to queue.");
        return paymentId;
    }

    private String reaproveitar(IdempotencyRecord anterior, String fingerprint) {
        if (!anterior.fingerprint().equals(fingerprint)) {
            throw new UnprocessableEntityException("Idempotency-Key já utilizada em outro pagamento.");
        }
        System.out.println("Idempotency-Key repetida: devolvendo o pagamento " + anterior.paymentId());
        return anterior.paymentId();
    }

    // O pagamento não entrou no fluxo: libera a chave para o cliente poder repetir a requisição.
    private void desfazerAceite(String idempotencyKey, Pagamento paymentEntity, RuntimeException falhaOriginal) {
        try {
            idempotencyRepository.liberar(idempotencyKey);
            paymentEntity.setStatus(StatusPagamento.FALHOU);
            paymentRepository.save(paymentEntity);
        } catch (RuntimeException falhaNaCompensacao) {
            falhaOriginal.addSuppressed(falhaNaCompensacao);
        }
    }

    private String fingerprintOf(Pagamento pagamento) {
        String canonico = pagamento.getSenderAccount() + "|" + pagamento.getReceiverPixKey() + "|"
                + pagamento.getAmount().stripTrailingZeros().toPlainString();
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(canonico.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 é obrigatório em toda JVM", e);
        }
    }
}
