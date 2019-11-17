/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package group.bison.test.logsystem.parse.jlogstash.core.metrics;

import group.bison.test.logsystem.parse.jlogstash.core.assembly.CmdLineParams;
import group.bison.test.logsystem.parse.jlogstash.core.metrics.groups.JlogstashJobMetricGroup;
import group.bison.test.logsystem.parse.jlogstash.core.metrics.groups.PipelineInputMetricGroup;
import group.bison.test.logsystem.parse.jlogstash.core.metrics.groups.PipelineOutputMetricGroup;
import group.bison.test.logsystem.parse.jlogstash.core.metrics.util.MetricUtils;
import group.bison.test.logsystem.parse.jlogstash.core.utils.LocalIpAddressUtil;

import java.util.List;
import java.util.Map;

/**
 * company: www.dtstack.com
 * author: toutian
 * create: 2019/7/30
 */
public class JlogstashMetric {

    private static List<Map> metrics;
    private static JlogstashMetric jlogstashMetric = new JlogstashMetric();


    private static MetricRegistryImpl metricRegistry;
    private static JlogstashJobMetricGroup jlogstashJobMetricGroup;
    private static PipelineInputMetricGroup pipelineInputMetricGroup;
    private static PipelineOutputMetricGroup pipelineOutputMetricGroup;


    public static JlogstashMetric getInstance(List<Map> m) {
        metrics = m;
        init();
        return jlogstashMetric;
    }



    public JlogstashMetric() {
    }

    private static void init(){
        String hostname = LocalIpAddressUtil.getLocalAddress();

        metricRegistry = new MetricRegistryImpl(metrics);
        jlogstashJobMetricGroup = MetricUtils.instantiateTaskManagerMetricGroup(metricRegistry);
        pipelineInputMetricGroup = new PipelineInputMetricGroup<>(metricRegistry, hostname, "input", "", CmdLineParams.getName());
        pipelineOutputMetricGroup = new PipelineOutputMetricGroup<>(metricRegistry, hostname, "output", "", CmdLineParams.getName());
    }

    public static JlogstashJobMetricGroup getJlogstashJobMetricGroup() {
        return jlogstashJobMetricGroup;
    }

    public static PipelineInputMetricGroup getPipelineInputMetricGroup() {
        return pipelineInputMetricGroup;
    }

    public static PipelineOutputMetricGroup getPipelineOutputMetricGroup() {
        return pipelineOutputMetricGroup;
    }

    public void close() {
        if (jlogstashJobMetricGroup != null) {
            jlogstashJobMetricGroup.close();
        }
        if (pipelineInputMetricGroup != null) {
            pipelineInputMetricGroup.close();
        }
        if (pipelineOutputMetricGroup != null) {
            pipelineOutputMetricGroup.close();
        }
        // metrics shutdown
        if (metricRegistry != null) {
            metricRegistry.shutdown();
            metricRegistry = null;
        }
    }
}
