/**
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package group.bison.test.logsystem.parse.jlogstash.pipeline.inputs.kafka10;

import group.bison.test.logsystem.parse.jlogstash.core.annotation.Required;
import group.bison.test.logsystem.parse.jlogstash.core.inputs.BaseInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

@SuppressWarnings("serial")
public class Kafka10 extends BaseInput {
    private static final Logger logger = LoggerFactory.getLogger(Kafka10.class);

    @Required(required = true)
    private static String topic;

    @Required(required = true)
    private static String groupId;

    @Required(required = true)
    private static String bootstrapServers;

    private static int threadCount = 1;

    @Required(required = true)
    private static Map<String, String> consumerSettings;

    private JKafkaConsumer consumer;

    public Kafka10(Map<String, Object> config) {
        super(config);
    }

    static {
        Thread.currentThread().setContextClassLoader(null);
    }

    @Override
    public void prepare() {
        Properties props = geneConsumerProp();

        props.put("bootstrap.servers", bootstrapServers);

        consumer = JKafkaConsumer.init(props);
    }

    private Properties geneConsumerProp() {
        Properties props = new Properties();

        Iterator<Entry<String, String>> consumerSetting = consumerSettings
                .entrySet().iterator();

        while (consumerSetting.hasNext()) {
            Entry<String, String> entry = consumerSetting.next();
            String k = entry.getKey();
            String v = entry.getValue();
            props.put(k, v);
        }

        return props;
    }

    @Override
    public void emit() {

        try {

            consumer.add(topic, groupId, new JKafkaConsumer.Caller() {

                @Override
                public void processMessage(String message) {
                    Map<String, Object> event = Kafka10.this.getDecoder().decode(message);
                    if (event != null && event.size() > 0) {
                        Kafka10.this.process(event);
                    }
                }

                @Override
                public void catchException(String message, Throwable e) {
                    logger.warn("kakfa consumer fetch is error,message={}", message);
                    logger.error("kakfa consumer fetch is error", e);
                }
            }, Integer.MAX_VALUE, threadCount).execute();

        } catch (Exception e) {
            logger.error("kafka emit error", e);
        }
    }

    @Override
    public void release() {
        consumer.close();
        logger.warn("input kafka release.");
    }

    public static void main(String[] args) {
        JKafkaConsumer.init("10.3.7.24:9992,10.3.7.44:9992,10.3.7.64:9992").add("log2kafka-system-testC", "test", new JKafkaConsumer.Caller() {

            @Override
            public void processMessage(String message) {
                System.out.println(message);
            }

            @Override
            public void catchException(String message, Throwable e) {
                e.printStackTrace();
            }
        }, Integer.MAX_VALUE, 1).execute();
    }

}
