package itau.gateway.queue.infrastructure.config;

import java.util.UUID;

import org.springframework.stereotype.Component;

import itau.gateway.queue.domain.port.out.IdGeneratorPort;

@Component
public class IdGenerator implements IdGeneratorPort {

    @Override
    public String generateId() {
        return UUID.randomUUID().toString();
    }
}
