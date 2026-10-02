package itau.persistence.queue.application.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import itau.persistence.queue.domain.port.in.PagamentoPersistenceUseCase;
import itau.persistence.queue.domain.port.out.ChavePixRepositoryPort;
import itau.persistence.queue.domain.port.out.PagamentoRepositoryPort;
import itau.persistence.queue.domain.model.ChavePix;
import itau.persistence.queue.domain.model.Pagamento;
import itau.persistence.queue.domain.model.PagamentoMessage;
import itau.pix.commons.enums.StatusPagamento;

@Service
public class PagamentoPersistenceUseCaseImpl implements PagamentoPersistenceUseCase {

    private final PagamentoRepositoryPort repository;
    private final ChavePixRepositoryPort chavePixRepository;

    public PagamentoPersistenceUseCaseImpl(PagamentoRepositoryPort repository, ChavePixRepositoryPort chavePixRepository) {
        this.repository = repository;
        this.chavePixRepository = chavePixRepository;
    }

    @Override
    @Transactional // utilizado para garantir que quando houver atualização de saldo das contas, tudo ocorra em uma transação unica sem perder a consistência.
    public void persist(PagamentoMessage message, boolean sucesso) {
        // Valida message caso tenha dado erro na fila
        if (message == null) {
            System.out.println("Mensagem de pagamento nula. Abortando persist.");
            return;
        }

        if (message.getId() == null) {
            System.out.println("Mensagem de pagamento sem ID. Abortando persist.");
            return;
        }

        // Idempotência: pagamento já finalizado (SUCESSO/FALHOU) não é reprocessado.
        Optional<Pagamento> existingPagamentoOpt = repository.findById(message.getId());
        if (existingPagamentoOpt.isPresent() && existingPagamentoOpt.get().isFinalizado()) {
            System.out.println("Pagamento " + message.getId() + " já finalizado como " + existingPagamentoOpt.get().getStatus() + ". Mensagem duplicada ignorada.");
            return;
        }

        // Valida amount caso venha zerado na fila
        BigDecimal amount = message.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            System.out.println("Valor do pagamento inválido (null ou <= 0) para id=" + message.getId());
            Pagamento invalid = buildPagamentoFromMessage(message, StatusPagamento.FALHOU, "Valor inválido");
            repository.save(invalid);
            return;
        }

        // reaproveita o pagamento existente (ainda PROCESSANDO), se não cria um novo
        Pagamento pagamento = existingPagamentoOpt.orElseGet(() -> buildPagamentoFromMessage(message, null, null));

        // Atualiza os campos
        pagamento.setAmount(amount);
        pagamento.setSenderAccount(message.getSenderAccount());
        pagamento.setReceiverPixKey(message.getReceiverPixKey());
        pagamento.setErrorDescription(message.getErrorDescription());

        // Caso o processamento externo marcou sucesso fazemos a transferência
        if (sucesso) {
            // Busca sender e receiver
            Optional<ChavePix> senderOpt = chavePixRepository.findByNumeroConta(message.getSenderAccount());
            Optional<ChavePix> receiverOpt = chavePixRepository.findByValorChave(message.getReceiverPixKey());

            if (senderOpt.isPresent() && receiverOpt.isPresent()) {
                ChavePix sender = senderOpt.get();
                ChavePix receiver = receiverOpt.get();

                // Verifica saldo suficiente caso tenha passado errado pelo worker
                if (sender.getSaldo().compareTo(amount) >= 0) {
                    // Atualiza saldos
                    sender.setSaldo(sender.getSaldo().subtract(amount));
                    receiver.setSaldo(receiver.getSaldo().add(amount));

                    chavePixRepository.save(sender);
                    chavePixRepository.save(receiver);

                    pagamento.setStatus(StatusPagamento.SUCESSO);
                    pagamento.setErrorDescription(null);
                    System.out.println("Pagamento " + pagamento.getId() + " aplicado: " + amount + " debitado de " + sender.getNumeroConta() + " e creditado em " + receiver.getNumeroConta());
                } else {// caso Saldo insuficiente
                    pagamento.setStatus(StatusPagamento.FALHOU);
                    pagamento.setErrorDescription("Saldo insuficiente");
                    System.out.println("Pagamento " + pagamento.getId() + " falhou: saldo insuficiente em conta " + sender.getNumeroConta());
                }

            } else {
                // Remetente ou destinatário não encontrados
                pagamento.setStatus(StatusPagamento.FALHOU);
                pagamento.setErrorDescription("Conta remetente ou chave destinatário não encontrada");
                System.out.println("Pagamento " + pagamento.getId() + " falhou: sender ou receiver não encontrados (senderPresent=" + senderOpt.isPresent() + ", receiverPresent=" + receiverOpt.isPresent() + ")");
            }

        } else {
            pagamento.setStatus(StatusPagamento.FALHOU);
            if (pagamento.getErrorDescription() == null || pagamento.getErrorDescription().isEmpty()) {
                pagamento.setErrorDescription("Processamento externo marcou falha");
            }
            System.out.println("Pagamento " + pagamento.getId() + " marcado como FALHOU por processamento externo.");
        }

        // Salva o pagamento por ultimo com o status correto (SUCESSO ou FALHOU)
        repository.save(pagamento);
    }

    private Pagamento buildPagamentoFromMessage(PagamentoMessage message, StatusPagamento status, String error) {
        Pagamento p = new Pagamento(
                message.getId(),
                message.getAmount(),
                message.getSenderAccount(),
                message.getReceiverPixKey(),
                status,
                error
        );
        return p;
    }
}
