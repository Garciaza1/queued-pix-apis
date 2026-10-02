package itau.gateway.queue.infrastructure.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import itau.gateway.queue.domain.exception.UnprocessableEntityException;
import itau.gateway.queue.domain.model.pagamento.Pagamento;
import itau.gateway.queue.domain.port.in.PagamentoUseCase;

@WebMvcTest(PagamentoController.class)
class PagamentoControllerTest {

    private static final String CORPO = """
            {"amount": 200.00, "senderAccount": "12345679", "receiverPixKey": "12345678802"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagamentoUseCase pagamentoUseCase;

    @Test
    void devolve202ComOIdDoPagamentoQuandoHaIdempotencyKey() throws Exception {
        when(pagamentoUseCase.processPayment(any(Pagamento.class), eq("chave-1"))).thenReturn("pagamento-1");

        mockMvc.perform(post("/api/pix/payments")
                        .header("Idempotency-Key", "chave-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.id").value("pagamento-1"));
    }

    @Test
    void rejeitaRequisicaoSemIdempotencyKey() throws Exception {
        mockMvc.perform(post("/api/pix/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pagamentoUseCase);
    }

    @Test
    void rejeitaIdempotencyKeyMaiorQue64Caracteres() throws Exception {
        mockMvc.perform(post("/api/pix/payments")
                        .header("Idempotency-Key", "x".repeat(65))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(pagamentoUseCase);
    }

    @Test
    void chaveReutilizadaEmOutroPagamentoViraUnprocessableEntity() throws Exception {
        when(pagamentoUseCase.processPayment(any(Pagamento.class), eq("chave-1")))
                .thenThrow(new UnprocessableEntityException("Idempotency-Key já utilizada em outro pagamento."));

        mockMvc.perform(post("/api/pix/payments")
                        .header("Idempotency-Key", "chave-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CORPO))
                .andExpect(status().isUnprocessableEntity());
    }
}
