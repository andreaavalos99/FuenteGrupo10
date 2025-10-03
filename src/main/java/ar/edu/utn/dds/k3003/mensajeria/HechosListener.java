package ar.edu.utn.dds.k3003.mensajeria;
import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Conditional(RabbitConfig.RabbitEnabled.class)
@Component
public class HechosListener {

    private final Channel channel;
    private final Fachada fachada;
    private final String queue;
    private final ObjectMapper mapper;

    public HechosListener(
            Channel channel,
            Fachada fachada,
            @Value("${QUEUE_NAME:hechos.nuevos}") String queue) {
        this.channel = channel;
        this.fachada = fachada;
        this.queue = queue;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        this.mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @PostConstruct
    void start() throws Exception {
        channel.basicQos(1);
        channel.basicConsume(queue, false, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String tag, Envelope env, AMQP.BasicProperties props, byte[] body) {
                try {
                    HechoDTO dto = mapper.readValue(body, HechoDTO.class);

                    fachada.altaHechoSinPublicar(dto); // <--- evita republish

                    channel.basicAck(env.getDeliveryTag(), false);
                    log.info("[mensajería] Hecho procesado OK id={}", dto.id());
                } catch (Exception e) {
                    log.warn("[mensajería] error procesando; requeue", e);
                    try { channel.basicNack(env.getDeliveryTag(), false, true); } catch (Exception ignore) {}
                }
            }
        });

        log.info("[mensajería] oyente iniciado en cola '{}'", queue);
    }
}