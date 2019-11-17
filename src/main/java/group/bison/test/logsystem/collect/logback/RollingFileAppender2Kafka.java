package group.bison.test.logsystem.collect.logback;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.rolling.RollingFileAppender;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import group.bison.test.logsystem.collect.LoggerNames;
import group.bison.test.logsystem.collect.config.Kafka4LogConfig;
import group.bison.test.logsystem.collect.parse.impl.CommonLogParser;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * Created by diaobisong on 2019/11/5.
 */
public class RollingFileAppender2Kafka extends RollingFileAppender {

    private KafkaProducer kafkaProducer = null;

    private String topic;

    public String getTopic() {
        String topicPrefix = StringUtils.isEmpty(topic) ? getName() : topic;
        String profile = Kafka4LogConfig.envProfile;
        return topicPrefix + "-" + (StringUtils.isEmpty(profile) ? "" : profile);
    }

    @Override
    public void start() {
        super.start();

        if (!Kafka4LogConfig.KafkaProvider4Log.getInstance().isProducerEnabled()) {
            return;
        }

        try {
            KafkaProducer kafkaProducer = Kafka4LogConfig.KafkaProvider4Log.getInstance().getKafkaProducer();
            kafkaProducer.flush();

            this.kafkaProducer = kafkaProducer;
        } catch (Exception e) {
            this.addError("KafkaRollingFileAppender start failed, name:" + getName(), e);
        }
    }

    @Override
    public void stop() {
        super.stop();

        if (!Kafka4LogConfig.KafkaProvider4Log.getInstance().isProducerEnabled()) {
            return;
        }

        try {
            kafkaProducer.close(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            this.addError("KafkaRollingFileAppender stop failed, name:" + getName(), e);
        }
    }

    @Override
    protected void subAppend(Object event) {
        Object originEvent = event;

        //日志预解析
        event = parse(event);
        boolean parsed = event != originEvent;

        //判断是否需脱敏
        boolean isNeedDesensiteLog = getIsNeedDesensiteLog(event);
        if (isNeedDesensiteLog) {
            //todo 脱敏
            event = event;
        }

        //判断是否可落盘
        boolean isCanWriteFile = getIsCanWriteFile(event);
        if (isCanWriteFile) {
            super.subAppend(event);
        }

        if (!Kafka4LogConfig.KafkaProvider4Log.getInstance().isProducerEnabled()) {
            return;
        } else if (kafkaProducer == null) {
            this.kafkaProducer = Kafka4LogConfig.KafkaProvider4Log.getInstance().getKafkaProducer();
        }

        try {
            String topic = getTopic();
            String key = String.join("", System.getenv("COMPUTERNAME"), "#", String.valueOf(System.currentTimeMillis()));
            String body = null;
            if (event instanceof String) {
                body = (String) event;
            } else if (event instanceof LoggingEvent) {
                if (parsed) {
                    body = ((LoggingEvent) event).getMessage();
                } else {
                    JSONObject eventObj = JSON.parseObject(JSON.toJSONString(event));
                    eventObj.put("level", ((LoggingEvent) event).getLevel());
                    eventObj.put("formattedMessage", ((LoggingEvent) event).getFormattedMessage());
                    body = ((LoggingEvent) event).getMessage();
                }
            } else {
                body = JSON.toJSONString(event);
            }
            ProducerRecord producerRecord = new ProducerRecord(topic, key, body);
            kafkaProducer.send(producerRecord);
        } catch (Exception e) {
            this.addInfo("KafkaRollingFileAppender subAppend failed, name:" + getName(), e);
        }
    }

    boolean getIsCanWriteFile(Object event) {
        return (event instanceof LoggingEvent) && ((LoggingEvent) event).getLoggerName().contains(LoggerNames.class.getName());
    }

    boolean getIsNeedDesensiteLog(Object event) {
        return (event instanceof LoggingEvent) && ((LoggingEvent) event).getLoggerName().equalsIgnoreCase(LoggerNames.DesensiteLoggerName.class.getName());
    }

    Object parse(Object event) {
        if (event instanceof String) {
            return event;
        }
        if (!(event instanceof LoggingEvent)) {
            return event;
        }

        LoggingEvent loggingEvent = (LoggingEvent) event;
        String loggerName = loggingEvent.getLoggerName();
        CommonLogParser commonLogParser = CommonLogParser.getInstance(loggerName);
        String parsedMessage = commonLogParser.parse(loggingEvent.getLevel().toString(), loggingEvent.getCallerData()[0].getClassName(), String.valueOf(loggingEvent.getTimeStamp()), loggingEvent.getMessage(), loggingEvent.getFormattedMessage());

        LoggingEvent parsedLoggingEvent = new LoggingEvent(null, (Logger) LoggerFactory.getLogger(loggingEvent.getLoggerName()), loggingEvent.getLevel(), parsedMessage, null, loggingEvent.getArgumentArray());
        return parsedLoggingEvent;
    }
}
