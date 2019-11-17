package group.bison.test.logsystem.collect.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by diaobisong on 2019/11/11.
 */
@Configuration
public class DissectParserConfig {

    @Bean
    public static LogPatternProvider logPatternProvider(@Value("${dissect.log.pattern:{}}") String logPattern) {
        LogPatternProvider logPatternProvider = LogPatternProvider.getInstance();
        logPatternProvider.setLoggerPatternMap(JSON.parseObject(logPattern, new TypeReference<Map<String, String>>() {
        }));
        return logPatternProvider;
    }

    public static class LogPatternProvider {

        private static final LogPatternProvider INSTANCE = new LogPatternProvider();

        private static Map<String, String> loggerPatternMap = new HashMap<>(16);

        void setLoggerPatternMap(Map<String, String> loggerPatternMap) {
            if (loggerPatternMap == null || loggerPatternMap.isEmpty()) {
                return;
            }

            this.loggerPatternMap.putAll(loggerPatternMap);
        }

        public String getPattern(String loggerName) {
            return loggerPatternMap.get(loggerName);
        }

        public static LogPatternProvider getInstance() {
            return INSTANCE;
        }
    }
}
