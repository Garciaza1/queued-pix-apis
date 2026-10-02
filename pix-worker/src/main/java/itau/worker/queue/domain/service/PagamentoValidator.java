package itau.worker.queue.domain.service;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;

import itau.worker.queue.domain.model.ChavePix;
import itau.worker.queue.domain.model.PagamentoMessage;

public class PagamentoValidator {

    public PagamentoValidationResult validate(PagamentoMessage message,
            Optional<ChavePix> senderOpt,
            Optional<ChavePix> receiverOpt) {
        if (message == null) {
            return PagamentoValidationResult.fail("Mensagem nula");
        }

        BigDecimal amount = message.getAmount();
        if (amount == null) {
            return PagamentoValidationResult.fail("Valor do pagamento nulo");
        }
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            return PagamentoValidationResult.fail("Valor do pagamento deve ser maior que zero");
        }

        if (!senderOpt.isPresent()) {
            return PagamentoValidationResult.fail("Conta do remetente não encontrada");
        }

        if (!receiverOpt.isPresent()) {
            return PagamentoValidationResult.fail("Chave Pix do destinatário não encontrada");
        }

        ChavePix sender = senderOpt.get();
        ChavePix receiver = receiverOpt.get();

        // se saldo for nulo, considerar ZERO (mas tratar como insuficiente)
        BigDecimal senderSaldo = sender.getSaldo() == null ? BigDecimal.ZERO : sender.getSaldo();

        if (senderSaldo.compareTo(amount) < 0) {
            return PagamentoValidationResult.fail("Saldo insuficiente");
        }

        // Evitar enviar para mesma conta: comparar agência e conta (tratando nulos)
        // depende da regra de negocio porque podemos ter uma mesma conta com duas agencias diferentes (transferencia entre contas)
        boolean sameAgencia = Objects.equals(sender.getNumeroAgencia(), receiver.getNumeroAgencia()); 
        boolean sameConta = Objects.equals(sender.getNumeroConta(), receiver.getNumeroConta());

        if (sameAgencia && sameConta) {
            return PagamentoValidationResult.fail("Não é permitido enviar Pix para a mesma conta");
        }

        // tudo ok
        return PagamentoValidationResult.ok();
    }
}
