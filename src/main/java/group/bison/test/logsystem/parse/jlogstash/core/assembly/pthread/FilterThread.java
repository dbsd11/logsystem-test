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
package group.bison.test.logsystem.parse.jlogstash.core.assembly.pthread;

import java.util.List;
import java.util.Map;

import group.bison.test.logsystem.parse.jlogstash.core.factory.LogstashThreadFactory;
import group.bison.test.logsystem.parse.jlogstash.core.filters.IBaseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import group.bison.test.logsystem.parse.jlogstash.core.assembly.qlist.QueueList;
import group.bison.test.logsystem.parse.jlogstash.core.exception.ExceptionUtil;
import group.bison.test.logsystem.parse.jlogstash.core.factory.FilterFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年11月29日 下午15:30:18
 * Company:www.dtstack.com
 *
 * @author sishu.yss
 */
public class FilterThread implements Runnable {

    private static Logger logger = LoggerFactory.getLogger(FilterThread.class);

    private BlockingQueue<Map<String, Object>> filterQueue;

    private static QueueList outPutQueueList;

    private List<IBaseFilter> filterProcessors;

    private static ExecutorService filterExecutor;

    public FilterThread(List<IBaseFilter> filterProcessors, BlockingQueue<Map<String, Object>> filterQueue) {
        this.filterProcessors = filterProcessors;
        this.filterQueue = filterQueue;
    }

    @SuppressWarnings("rawtypes")
    public static void initFilterThread(List<Map> filters, QueueList filterQueueList, QueueList outPutQueueList) throws Exception {
        if (filterExecutor == null) {
            int size = filterQueueList.getQueueList().size();
            filterExecutor = new ThreadPoolExecutor(size, size,
                    0L, TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<Runnable>(), new LogstashThreadFactory(FilterThread.class.getName()));
        }
        FilterThread.outPutQueueList = outPutQueueList;
        for (BlockingQueue<Map<String, Object>> queueList : filterQueueList.getQueueList()) {
            List<IBaseFilter> baseFilters = FilterFactory.getBatchInstance(filters);
            filterExecutor.submit(new FilterThread(baseFilters, queueList));
        }
    }

    @Override
    public void run() {
        A:
        while (true) {
            Map<String, Object> event = null;
            try {
                event = this.filterQueue.take();
                if (filterProcessors != null) {
                    for (IBaseFilter bf : filterProcessors) {
                        if (event == null || event.size() == 0) {
                            continue A;
                        }
                        bf.process(event);
                    }
                }
                if (event != null) {
                    outPutQueueList.put(event);
                }
            } catch (Exception e) {
                logger.error("{}:filter event failed:{}", event, ExceptionUtil.getErrorMessage(e));
            }
        }
    }
}
