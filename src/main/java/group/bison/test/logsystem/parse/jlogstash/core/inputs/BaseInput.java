/**
 * Licensed to the Apache Software Foundation (ASF) under one
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
package group.bison.test.logsystem.parse.jlogstash.core.inputs;


/**
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:27:03
 * Company: www.dtstack.com
 *
 * @author sishu.yss
 */

import java.util.Map;

import group.bison.test.logsystem.parse.jlogstash.core.decoder.JsonMessageDecoder;
import group.bison.test.logsystem.parse.jlogstash.core.metrics.JlogstashMetric;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import group.bison.test.logsystem.parse.jlogstash.core.assembly.qlist.QueueList;
import group.bison.test.logsystem.parse.jlogstash.core.decoder.IDecode;
import group.bison.test.logsystem.parse.jlogstash.core.decoder.JsonDecoder;
import group.bison.test.logsystem.parse.jlogstash.core.decoder.MultilineDecoder;
import group.bison.test.logsystem.parse.jlogstash.core.decoder.PlainDecoder;
import group.bison.test.logsystem.parse.jlogstash.core.utils.BasePluginUtil;

@SuppressWarnings("serial")
public abstract class BaseInput implements IBaseInput, java.io.Serializable {

    private static final Logger baseLogger = LoggerFactory.getLogger(BaseInput.class);

    protected Map<String, Object> config;

    private IDecode decoder;

    private static QueueList inputQueueList;

    protected Map<String, Object> addFields = null;

    protected static BasePluginUtil basePluginUtil = new BasePluginUtil();

    private IDecode createDecoder() {
        String codec = (String) this.config.get("codec");
        if ("json_message".equalsIgnoreCase(codec)) {
            return new JsonMessageDecoder();
        } else if ("multiline".equalsIgnoreCase(codec)) {
            return createMultiLineDecoder(config);
        } else if ("plain".equalsIgnoreCase(codec)) {
            return new PlainDecoder();
        } else {
            return new JsonDecoder();
        }
    }

    public IDecode getDecoder() {
        return decoder;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public IDecode createMultiLineDecoder(Map config) {

        if (config.get("multiline") == null) {
            baseLogger.error("multiline decoder need to set multiline param.");
            System.exit(-1);
        }

        Map<String, Object> codecConfig = (Map<String, Object>) config.get("multiline");

        if (codecConfig.get("pattern") == null || codecConfig.get("what") == null) {
            baseLogger.error("multiline decoder need to set param (pattern and what)");
            System.exit(-1);
        }

        String patternStr = (String) codecConfig.get("pattern");
        String what = (String) codecConfig.get("what");
        boolean negate = false;

        if (codecConfig.get("negate") != null) {
            negate = (boolean) codecConfig.get("negate");
        }

        return new MultilineDecoder(patternStr, what, negate, inputQueueList);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public BaseInput(Map config) {
        this.config = config;
        decoder = createDecoder();
        if (this.config != null) {
            addFields = (Map<String, Object>) this.config.get("addFields");
        }
    }

    public void process(Map<String, Object> event) {
        if (event != null && event.size() > 0) {
            if (addFields != null) {
                basePluginUtil.addFields(event, addFields);
            }
            inputQueueList.put(event);
            if (JlogstashMetric.getPipelineInputMetricGroup() != null) {
                JlogstashMetric.getPipelineInputMetricGroup().getNumRecordsInCounter().inc();
                JlogstashMetric.getPipelineInputMetricGroup().getNumBytesInLocalCounter().inc(ObjectSizeCalculator.getObjectSize(event));
            }
        }
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public static void setInputQueueList(QueueList inputQueueList) {
        BaseInput.inputQueueList = inputQueueList;
    }

    public static QueueList getInputQueueList() {
        return inputQueueList;
    }

}
