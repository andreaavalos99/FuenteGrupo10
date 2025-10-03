package ar.edu.utn.dds.k3003.mensajeria;

import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.io.IOException;

@Configuration
public class RabbitConfig {

    @Value("${RABBITMQ_URI:}") private String uri;
    @Value("${QUEUE_NAME:hechos.nuevos}") private String queue;

    @Bean
    @Conditional(RabbitEnabled.class)
    public com.rabbitmq.client.Connection rabbitConnection() throws Exception {
        ConnectionFactory f = new ConnectionFactory();
        if (uri == null || uri.isBlank()) throw new IllegalStateException("RABBITMQ_URI vacío");
        f.setUri(uri);
        f.setAutomaticRecoveryEnabled(true);
        return f.newConnection("fuentes-app");
    }

    @Bean(destroyMethod = "close")
    @Conditional(RabbitEnabled.class)
    public com.rabbitmq.client.Channel rabbitChannel(com.rabbitmq.client.Connection c) throws Exception {
        var ch = c.createChannel();
        try {
            ch.queueDeclarePassive(queue);
        } catch (IOException notExistsOrMismatch) {
            ch.queueDeclare(queue, true, false, false, null);
        }
        return ch;
    }

    public static class RabbitEnabled implements Condition {
        @Override public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
            var v = System.getenv("RABBITMQ_URI");
            return v != null && !v.isBlank();
        }
    }
}
