package group.bison.test.logsystem.collect.config;

import group.bison.test.logsystem.collect.logback.LogConsumer4Kafka;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Created by diaobisong on 2019/11/5.
 */
@Configuration
@ConditionalOnProperty(
        name = {"spring.kafka4log.enabled"},
        matchIfMissing = true
)
public class Kafka4LogConfig {

    @Autowired(required = false)
    private Optional<KafkaProperties> kafkaPropertiesOptional;

    @Autowired(required = false)
    private Optional<KafkaProperties4Log> kafkaProperties4LogOptional;

    public static String envProfile;

    @Value("${spring.profiles.active:}")
    public void setEnvProfile(String envProfile) {
        Kafka4LogConfig.envProfile = envProfile;
    }

    @Bean
    public KafkaProvider4Log kafkaProvider4Log() {
        KafkaProvider4Log kafkaProvider = KafkaProvider4Log.getInstance();

        boolean isPresentKafkaProperties = kafkaPropertiesOptional.isPresent() ? !kafkaPropertiesOptional.get().getBootstrapServers().contains("localhost:9092") : false;

        Map<String, Object> kafkaConsumerPropertiesMap = isPresentKafkaProperties ? kafkaPropertiesOptional.get().buildConsumerProperties() : kafkaProperties4LogOptional.isPresent() ? kafkaProperties4LogOptional.get().buildConsumerProperties() : Collections.emptyMap();
        if (kafkaConsumerPropertiesMap.containsKey(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG)) {
            if (!kafkaConsumerPropertiesMap.containsKey("groupId")) {
                kafkaConsumerPropertiesMap.put("groupId", "logsystem");
            }
            if (!kafkaConsumerPropertiesMap.containsKey("autoCommitInterval")) {
                kafkaConsumerPropertiesMap.put("autoCommitInterval", "30000");
            }
            if (!kafkaConsumerPropertiesMap.containsKey("enableAutoCommit")) {
                kafkaConsumerPropertiesMap.put("enableAutoCommit", "true");
            }

            KafkaConsumer kafkaConsumer = new KafkaConsumer(kafkaConsumerPropertiesMap);
            kafkaProvider.setKafkaConsumer(kafkaConsumer);

            kafkaProvider.enableConsumer(true);

            //启动异步拉取kafka中LogMessage任务
            CompletableFuture.runAsync(new KafkaLogMessageRunner()).exceptionally((exception) -> {
                Logger LOG = LoggerFactory.getLogger("kafkaLogMessageRunner");
                LOG.error("启动KafkaLogMessageRunner失败", exception);
                return null;
            });
        }

        Map<String, Object> kafkaProducerPropertiesMap = isPresentKafkaProperties ? kafkaPropertiesOptional.get().buildProducerProperties() : kafkaProperties4LogOptional.isPresent() ? kafkaProperties4LogOptional.get().buildProducerProperties() : Collections.emptyMap();
        if (kafkaProducerPropertiesMap.containsKey(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG)) {
            KafkaProducer kafkaProducer = new KafkaProducer(kafkaProducerPropertiesMap);
            kafkaProvider.setKafkaProducer(kafkaProducer);

            kafkaProvider.enableProducer(true);
        }

        return kafkaProvider;
    }

    public static class KafkaProvider4Log {

        private static final KafkaProvider4Log INSTANCE = new KafkaProvider4Log();

        private KafkaConsumer kafkaConsumer;

        private KafkaProducer kafkaProducer;

        private AtomicBoolean consumerEnabled = new AtomicBoolean(false);

        private AtomicBoolean producerEnabled = new AtomicBoolean(false);

        private List<MessageListener> messageListenerList = new LinkedList<>();

        private KafkaProvider4Log() {
        }

        public KafkaConsumer getKafkaConsumer() {
            return kafkaConsumer;
        }

        public void setKafkaConsumer(KafkaConsumer kafkaConsumer) {
            this.kafkaConsumer = kafkaConsumer;
        }

        public KafkaProducer getKafkaProducer() {
            return kafkaProducer;
        }

        public void setKafkaProducer(KafkaProducer kafkaProducer) {
            this.kafkaProducer = kafkaProducer;
        }

        public void enableProducer(boolean enable) {
            producerEnabled.set(enable);
        }

        public void enableConsumer(boolean enable) {
            consumerEnabled.set(enable);
        }

        public boolean isProducerEnabled() {
            return producerEnabled.get();
        }

        public boolean isConsumerEnabled() {
            return consumerEnabled.get();
        }

        public List<MessageListener> getMessageListenerList() {
            return messageListenerList;
        }

        public void addMessageListener(MessageListener messageListener) {
            if (!messageListenerList.contains(messageListener)) {
                messageListenerList.add(messageListener);
            }
        }

        public static KafkaProvider4Log getInstance() {
            return INSTANCE;
        }
    }

    public static class KafkaLogMessageRunner implements Runnable {

        private static final Logger LOG = LoggerFactory.getLogger("kafkaLogMessageRunner");

        @Override
        public void run() {
            try {
                Thread.sleep(10000);
            } catch (Exception e) {
            }

            Set<String> topicList = KafkaProvider4Log.getInstance().getMessageListenerList().stream().filter(messageListener -> messageListener instanceof LogConsumer4Kafka).map(messageListener1 -> ((LogConsumer4Kafka) messageListener1).getTopic()).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(topicList)) {
                LOG.info("topicList isEmpty，skip pollMessage");
                return;
            }

            KafkaConsumer kafkaConsumer = KafkaProvider4Log.getInstance().getKafkaConsumer();
            kafkaConsumer.subscribe(topicList);

            LOG.info("开始pollMessage, topicNum:{}", topicList.size());

            while (true) {
                if (!KafkaProvider4Log.getInstance().isConsumerEnabled()) {
                    continue;
                }

                try {
                    pollMessage();

                    Thread.sleep(1000);
                } catch (Exception e) {
                    LOG.error("KafkaLogMessageRunner pull failed", e);
                } finally {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                    }
                }
            }
        }

        void pollMessage() throws Exception {
            KafkaConsumer kafkaConsumer = KafkaProvider4Log.getInstance().getKafkaConsumer();
            List<MessageListener> messageListenerList = KafkaProvider4Log.getInstance().getMessageListenerList();

            ConsumerRecords consumerRecords = kafkaConsumer.poll(3000);

            messageListenerList.stream().filter(messageListener -> messageListener instanceof LogConsumer4Kafka).forEach(messageListener -> {
                Iterable<ConsumerRecord> consumerRecordIterator = consumerRecords.records(((LogConsumer4Kafka) messageListener).getTopic());
                consumerRecordIterator.forEach(consumerRecord -> messageListener.onMessage(consumerRecord));
            });
        }
    }
}
