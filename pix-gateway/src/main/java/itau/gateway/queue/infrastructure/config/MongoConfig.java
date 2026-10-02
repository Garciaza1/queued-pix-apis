package itau.gateway.queue.infrastructure.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> builder.applyToConnectionPoolSettings(poolBuilder -> {
            poolBuilder.maxSize(50);      // máximo de conexões no pool
            poolBuilder.minSize(5);       // mínimo de conexões abertas
            poolBuilder.maxWaitTime(1000, TimeUnit.MILLISECONDS); // tempo máximo de espera
        });
    }
}
