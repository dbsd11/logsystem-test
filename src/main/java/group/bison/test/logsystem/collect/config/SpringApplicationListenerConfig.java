package group.bison.test.logsystem.collect.config;

import group.bison.test.logsystem.collect.LoggerNames;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.ContextStoppedEvent;
import org.springframework.stereotype.Component;

/**
 * Created by diaobisong on 2019/11/7.
 */
@Component
public class SpringApplicationListenerConfig {

    @Bean
    public SpringApplicationListener springApplicationListener() {
        return new SpringApplicationListener();
    }

    public static class SpringApplicationListener implements ApplicationListener<ApplicationEvent> {

        private static Logger LOG2KAFKA = LoggerFactory.getLogger(LoggerNames.SystemLoggerName.class);

        @Override
        public void onApplicationEvent(ApplicationEvent event) {
            if (event instanceof ApplicationStartedEvent) {
                LOG2KAFKA.info("application:{} started", ((ApplicationStartedEvent) event).getApplicationContext().getId());
            }
            if (event instanceof ContextClosedEvent) {
                LOG2KAFKA.info("application:{} closed", ((ContextClosedEvent) event).getApplicationContext().getId());
            }
            if (event instanceof ContextStoppedEvent) {
                LOG2KAFKA.info("application:{} stoped", ((ContextStoppedEvent) event).getApplicationContext().getId());
            }
        }
    }
}
