package ar.edu.utn.dds.k3003.mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Component
@Conditional(RabbitConfig.RabbitEnabled.class)
public class HechosPublisher {

    private final Channel channel;
    private final ObjectMapper mapper;
    private final Counter publicadosOk, publicadosError;

    @Value("${MSG_EXCHANGE:eventos}")      private String exchange;
    @Value("${MSG_RK_HECHO:hecho.creado}") private String rkHecho;

    public HechosPublisher(Channel ch, MeterRegistry mr) {
        this.channel = ch;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.publicadosOk    = Counter.builder("fuentes.msg.publicados.ok").register(mr);
        this.publicadosError = Counter.builder("fuentes.msg.publicados.error").register(mr);
    }

    public void publicarHechoCreado(Object payload) {
        try {
            String messageId = UUID.randomUUID().toString();
            byte[] body = mapper.writeValueAsBytes(payload);
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .appId("fuente")
                    .messageId(messageId)
                    .timestamp(new Date())
                    .contentType("application/json")
                    .deliveryMode(2)
                    .headers(Map.of("origin","fuente","eventType","hecho.creado"))
                    .build();
            channel.basicPublish(exchange, rkHecho, props, body);
            publicadosOk.increment();
        } catch (Exception e) {
            publicadosError.increment();
        }
    }

    public void publicarRaw(String routingKey, String payload) {
        try {
            AMQP.BasicProperties props = new AMQP.BasicProperties.Builder()
                    .appId("fuente").messageId(UUID.randomUUID().toString())
                    .timestamp(new Date()).contentType("text/plain").deliveryMode(2).build();
            channel.basicPublish(exchange, routingKey, props, payload.getBytes(StandardCharsets.UTF_8));
            publicadosOk.increment();
        } catch (Exception e) {
            publicadosError.increment();
        }
    }
}
