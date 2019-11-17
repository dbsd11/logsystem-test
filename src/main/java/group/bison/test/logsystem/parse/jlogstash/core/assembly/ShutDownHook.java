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
package group.bison.test.logsystem.parse.jlogstash.core.assembly;

import java.util.List;

import group.bison.test.logsystem.parse.jlogstash.core.inputs.IBaseInput;
import group.bison.test.logsystem.parse.jlogstash.core.metrics.JlogstashMetric;
import group.bison.test.logsystem.parse.jlogstash.core.outputs.IBaseOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import group.bison.test.logsystem.parse.jlogstash.core.assembly.qlist.QueueList;

/**
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:25:34
 * Company: www.dtstack.com
 *
 * @author sishu.yss
 */
public class ShutDownHook {

    private Logger logger = LoggerFactory.getLogger(ShutDownHook.class);

    private QueueList initFilterQueueList;

    private QueueList initOutputQueueList;

    private List<IBaseInput> baseInputs;

    private List<IBaseOutput> baseOutputs;

    private JlogstashMetric jlogstashMetric;

    public ShutDownHook(QueueList initFilterQueueList,
                        QueueList initOutputQueueList,
                        List<IBaseInput> baseInputs,
                        List<IBaseOutput> baseOutputs,
                        JlogstashMetric jlogstashMetric) {
        this.initFilterQueueList = initFilterQueueList;
        this.initOutputQueueList = initOutputQueueList;
        this.baseInputs = baseInputs;
        this.baseOutputs = baseOutputs;
        this.jlogstashMetric = jlogstashMetric;
    }

    public void addShutDownHook() {
        Thread shut = new Thread(new ShutDownHookThread());
        shut.setDaemon(true);
        Runtime.getRuntime().addShutdownHook(shut);
        logger.debug("addShutDownHook success ...");
    }

    class ShutDownHookThread implements Runnable {
        private void inputRelease() {
            try {
                if (baseInputs != null) {
                    for (IBaseInput input : baseInputs) {
                        input.release();
                    }
                }
                logger.warn("inputRelease success...");
            } catch (Exception e) {
                logger.error("inputRelease error:{}", e.getMessage());
            }
        }

        private void outPutRelease() {
            try {
                if (baseOutputs != null) {
                    for (IBaseOutput outPut : baseOutputs) {
                        outPut.release();
                    }
                }
                logger.warn("outPutRelease success...");
            } catch (Exception e) {
                logger.error("outPutRelease error:{}", e.getMessage());
            }
        }

        private void metricsRelease() {
            if (jlogstashMetric != null) {
                jlogstashMetric.close();
            }
        }

        @Override
        public void run() {
            inputRelease();
            if (initFilterQueueList != null) {
                initFilterQueueList.queueRelease();
            }
            if (initOutputQueueList != null) {
                initOutputQueueList.queueRelease();
            }
            outPutRelease();
            metricsRelease();
        }
    }
}
