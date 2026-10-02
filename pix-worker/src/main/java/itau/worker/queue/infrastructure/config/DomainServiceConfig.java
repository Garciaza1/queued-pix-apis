package itau.worker.queue.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import itau.worker.queue.domain.service.PagamentoValidator;

@Configuration
public class DomainServiceConfig {

    @Bean
    public PagamentoValidator pagamentoValidator() {
        return new PagamentoValidator();
    }
}
