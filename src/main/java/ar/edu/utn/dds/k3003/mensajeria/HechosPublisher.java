package ar.edu.utn.dds.k3003.mensajeria;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

@Component
@Conditional(RabbitConfig.RabbitEnabled.class)
public class HechosPublisher {
    private final Channel channel;
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule())
            .disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final String queue;

    public HechosPublisher(Channel channel, @Value("${QUEUE_NAME:hechos.nuevos}") String queue) {
        this.channel = channel;
        this.queue = queue;
    }

    public void publicar(Object payload) throws Exception {
        var props = new AMQP.BasicProperties.Builder()
                .contentType("application/json")
                .deliveryMode(2)
                .build();
        byte[] body = mapper.writeValueAsBytes(payload);
        channel.basicPublish("", queue, props, body); // default exchange
    }
}

