package group.bison.test.logsystem.collect.logback;

import group.bison.test.logsystem.collect.config.Kafka4LogConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.util.StringUtils;

/**
 * Created by diaobisong on 2019/11/5.
 */
public abstract class LogConsumer4Kafka implements MessageListener<String, String> {

    private Logger LOG = LoggerFactory.getLogger(getClass());

    private String name;

    private String topic;

    private KafkaProducer retryKafkaProducer;

    public LogConsumer4Kafka(String name) {
        this.name = name;

        this.retryKafkaProducer = Kafka4LogConfig.KafkaProvider4Log.getInstance().getKafkaProducer();

        Kafka4LogConfig.KafkaProvider4Log.getInstance().addMessageListener(this);
    }

    public String getName() {
        return name;
    }

    public String getTopic() {
        String topicPrefix = (StringUtils.isEmpty(topic) ? getName() : topic);
        String profile = Kafka4LogConfig.envProfile;
        return topicPrefix + "-" + (StringUtils.isEmpty(profile) ? "" : profile);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public void onMessage(ConsumerRecord<String, String> data) {
        try {
            dpProcessLog(data.key(), data.value());
        } catch (Exception e) {
            LOG.info("processLog失败 name:{}, topic:{}", getName(), getTopic(), e);

            try {
                if (Kafka4LogConfig.KafkaProvider4Log.getInstance().isProducerEnabled()) {
                    String retryTopic = getTopic() + "-Retry";
                    ProducerRecord producerRecord = new ProducerRecord(retryTopic, data.key(), data.value());
                    retryKafkaProducer.send(producerRecord);
                }

                LOG.info("retryKafkaProducer发送成功 name:{}, topic:{}", getName(), getTopic() + "-Retry", e);
            } catch (Exception e1) {
                LOG.error("retryKafkaProducer发送失败 name:{}, topic:{}， 丢弃", getName(), getTopic() + "-Retry", e);
            }
        }
    }

    public abstract void dpProcessLog(String eventTimestamp, Object event) throws Exception;
}
