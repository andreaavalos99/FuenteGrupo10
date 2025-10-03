package ar.edu.utn.dds.k3003.mensajeria;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.core.type.AnnotatedTypeMetadata;

import com.rabbitmq.client.*;

@Configuration
public class RabbitConfig {

    @Value("${RABBITMQ_URI:}")
    private String rabbitUri;

    @Value("${MSG_EXCHANGE:eventos}")
    public String EXCHANGE_EVENTOS;

    @Value("${MSG_DLX:eventos.dlx}")
    public String DLX;

    @Value("${MSG_RK_HECHO:hecho.creado}")
    public String RK_HECHO_CREADO;

    @Bean(destroyMethod = "close")
    @Conditional(RabbitEnabled.class)
    public Connection rabbitConnection() throws Exception {
        if (rabbitUri == null || rabbitUri.isBlank())
            throw new IllegalStateException("RABBITMQ_URI vacío");
        ConnectionFactory f = new ConnectionFactory();
        f.setUri(rabbitUri);
        f.setAutomaticRecoveryEnabled(true);
        return f.newConnection("fuente");
    }

    @Bean(destroyMethod = "close")
    @Conditional(RabbitEnabled.class)
    public Channel rabbitChannel(Connection c) throws Exception {
        Channel ch = c.createChannel();
        ch.exchangeDeclare(EXCHANGE_EVENTOS, BuiltinExchangeType.TOPIC, true);
        ch.exchangeDeclare(DLX, BuiltinExchangeType.TOPIC, true);
        return ch;
    }

    public static class RabbitEnabled implements Condition {
        @Override public boolean matches(ConditionContext ctx, AnnotatedTypeMetadata md) {
            String v = System.getenv("RABBITMQ_URI");
            return v != null && !v.isBlank();
        }
    }
}
