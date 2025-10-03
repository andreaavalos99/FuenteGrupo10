package ar.edu.utn.dds.k3003.mensajeria;


import ar.edu.utn.dds.k3003.app.Fachada;
import ar.edu.utn.dds.k3003.repository.ProcessedMessageRepository;
import ar.edu.utn.dds.k3003.facades.dtos.HechoDTO;
import ar.edu.utn.dds.k3003.model.Suscripcion;
import ar.edu.utn.dds.k3003.repository.SuscripcionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rabbitmq.client.*;
import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;


import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Conditional(RabbitConfig.RabbitEnabled.class)
public class SubscriptionManager {

    private final Channel ch;
    private final SuscripcionRepository susRepo;
    private final ProcessedMessageRepository msgRepo;
    private final Fachada fachada;
    private final ObjectMapper mapper;

    private final Counter msgRecibidos;
    private final Counter msgProcesados;
    private final Counter msgFallidos;
    private final DistributionSummary lagMs;

    @Value("${MSG_DLX:eventos.dlx}") private String dlx;
    @Value("${MSG_DLQ_PREFIX:dlq.}") private String dlqPrefix;
    @Value("${MSG_Q_PREFIX:fuente.}") private String qPrefix;
    @Value("${MSG_Q_SUFFIX:.in}") private String qSuffix;

    private final Map<String, String> consumerTagsByQueue = new ConcurrentHashMap<>();

    public SubscriptionManager(Channel ch,
                               SuscripcionRepository susRepo,
                               ProcessedMessageRepository msgRepo,
                               Fachada fachada,
                               MeterRegistry mr) {
        this.ch = ch;
        this.susRepo = susRepo;
        this.msgRepo = msgRepo;
        this.fachada = fachada;
        this.mapper = new ObjectMapper().registerModule(new JavaTimeModule());

        this.msgRecibidos  = Counter.builder("fuentes.msg.recibidos").register(mr);
        this.msgProcesados = Counter.builder("fuentes.msg.procesados").register(mr);
        this.msgFallidos   = Counter.builder("fuentes.msg.fallidos").register(mr);
        this.lagMs         = DistributionSummary.builder("fuentes.msg.lag.ms").register(mr);
    }

    @PostConstruct
    public void initExisting() throws Exception {
        for (Suscripcion s : susRepo.findAll()) {
            declareQueueAndBind(s.getQueueName(), s.getTopic());
            startConsumer(s.getQueueName());
        }
        log.info("[mq] {} suscripciones restauradas", consumerTagsByQueue.size());
    }

    public Suscripcion subscribeTopic(String topic) throws Exception {
        String queue = qPrefix + topic.replace('*','_').replace('#','_') + qSuffix;

        declareQueueAndBind(queue, topic);
        startConsumer(queue);

        Suscripcion s = new Suscripcion();
        s.setTopic(topic);
        s.setQueueName(queue);
        s = susRepo.save(s);

        log.info("[mq] suscripto topic='{}' queue='{}'", topic, queue);
        return s;
    }

    public void unsubscribe(String id) throws Exception {
        Suscripcion s = susRepo.findById(id).orElseThrow();
        stopConsumer(s.getQueueName());

        if (s.getQueueName().startsWith(qPrefix)) {
            ch.queueUnbind(s.getQueueName(), getExchangeName(), s.getTopic());
            ch.queueDelete(s.getQueueName());
        }
        susRepo.deleteById(id);
        log.info("[mq] desuscripto topic='{}' queue='{}'", s.getTopic(), s.getQueueName());
    }

    private String getExchangeName() {
     return System.getenv().getOrDefault("MSG_EXCHANGE","eventos");
    }

    private void declareQueueAndBind(String queue, String topic) throws IOException {
        String rkDlq = dlqPrefix + topic;
        Map<String,Object> args = Map.of(
                "x-dead-letter-exchange", dlx,
                "x-dead-letter-routing-key", rkDlq,
                "x-message-ttl", 600_000 // 10 min
        );
        ch.queueDeclare(queue, true, false, false, args);
        ch.queueBind(queue, getExchangeName(), topic);
        ch.queueBind(dlqNameFor(queue), dlx, rkDlq); // aseguro DLQ
    }

    private String dlqNameFor(String queue) throws IOException {
        String dlq = queue + ".dlq";
        ch.queueDeclare(dlq, true, false, false, null);
        return dlq;
    }

    private void startConsumer(String queue) throws IOException {
        ch.basicQos(1);
        String tag = ch.basicConsume(queue, false, new DefaultConsumer(ch) {
            @Override
            public void handleDelivery(String consumerTag, Envelope env, AMQP.BasicProperties props, byte[] body) throws IOException {
                msgRecibidos.increment();
                String messageId = props.getMessageId();
                String eventType = header(props, "eventType");
                String origin    = header(props, "origin");
                try {
                    // Lag (si viene timestamp)
                    if (props.getTimestamp() != null) {
                        long lag = Duration.between(props.getTimestamp().toInstant(), Instant.now()).toMillis();
                        lagMs.record(lag);
                    }

                    // Solo eventos esperados
                    if (eventType != null && !eventType.equals("hecho.creado") && !eventType.equals("hechos.nuevos")) {
                        ch.basicAck(env.getDeliveryTag(), false);
                        return;
                    }

                    // Evito eco
                    if ("fuente".equalsIgnoreCase(origin)) {
                        ch.basicAck(env.getDeliveryTag(), false);
                        return;
                    }

                    // Idempotencia persistente
                    if (messageId != null && msgRepo.existsById(messageId)) {
                        ch.basicAck(env.getDeliveryTag(), false);
                        return;
                    }

                    // Parseo y alta
                    HechoDTO dto = tryParse(body);
                    fachada.altaHechoSinPublicar(dto);

                    if (messageId != null) msgRepo.save(new ProcessedMessage(messageId));
                    msgProcesados.increment();
                    ch.basicAck(env.getDeliveryTag(), false);
                } catch (Exception ex) {
                    msgFallidos.increment();
                    // redelivery simple → requeue una vez; luego DLQ
                    if (env.isRedeliver()) ch.basicReject(env.getDeliveryTag(), false);
                    else ch.basicNack(env.getDeliveryTag(), false, true);
                }
            }
        });
        consumerTagsByQueue.put(queue, tag);
    }

    private void stopConsumer(String queue) throws IOException {
        String tag = consumerTagsByQueue.remove(queue);
        if (tag != null) ch.basicCancel(tag);
    }

    private String header(AMQP.BasicProperties props, String key) {
        if (props.getHeaders() == null) return null;
        Object v = props.getHeaders().get(key);
        return v == null ? null : String.valueOf(v);
    }

    private HechoDTO tryParse(byte[] body) throws IOException {
        String s = new String(body);
        if (s.trim().startsWith("{")) return mapper.readValue(body, HechoDTO.class);
        throw new IOException("Payload inválido para HechoDTO: " + s);
    }
}

