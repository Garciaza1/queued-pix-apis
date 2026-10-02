package itau.gateway.queue.infrastructure.config;

import java.util.concurrent.TimeUnit;

import org.springframework.boot.autoconfigure.mongo.MongoClientSettingsBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions.BigDecimalRepresentation;

@Configuration
public class MongoConfig {

    // Decimal128 (e não String) para o Mongo conseguir $inc/$gte em saldo; as 3 APIs dividem as coleções e precisam concordar.
    @Bean
    public MongoCustomConversions mongoCustomConversions() {
        return MongoCustomConversions.create(adapter -> adapter.bigDecimal(BigDecimalRepresentation.DECIMAL128));
    }

    @Bean
    public MongoClientSettingsBuilderCustomizer mongoClientSettingsBuilderCustomizer() {
        return builder -> builder.applyToConnectionPoolSettings(poolBuilder -> {
            poolBuilder.maxSize(50);      // máximo de conexões no pool
            poolBuilder.minSize(5);       // mínimo de conexões abertas
            poolBuilder.maxWaitTime(1000, TimeUnit.MILLISECONDS); // tempo máximo de espera
        });
    }
}
