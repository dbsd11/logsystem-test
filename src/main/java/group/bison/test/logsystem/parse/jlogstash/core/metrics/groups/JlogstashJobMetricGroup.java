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

package group.bison.test.logsystem.parse.jlogstash.core.metrics.groups;


import group.bison.test.logsystem.parse.jlogstash.core.metrics.base.CharacterFilter;
import group.bison.test.logsystem.parse.jlogstash.core.metrics.MetricRegistry;
import group.bison.test.logsystem.parse.jlogstash.core.metrics.scope.ScopeFormat;

import java.util.Collections;
import java.util.Map;

/**
 * copy from https://github.com/apache/flink
 * <p>
 * Special {@link group.bison.test.logsystem.parse.jlogstash.core.metrics.base.MetricGroup} representing a JobManager.
 * <p>
 * <p>Contains extra logic for adding jobs with tasks, and removing jobs when they do
 * not contain tasks any more
 */
public class JlogstashJobMetricGroup extends ComponentMetricGroup<JlogstashJobMetricGroup> {

    private final String hostname;
    private final String jobName;

    public JlogstashJobMetricGroup(MetricRegistry registry, String hostname, String jobName) {
        super(registry, registry.getScopeFormats().getJlogstashJobScopeFormat().formatScope(hostname, jobName), null);
        this.hostname = hostname;
        this.jobName = jobName;
    }

    // ------------------------------------------------------------------------
    //  Component Metric Group Specifics
    // ------------------------------------------------------------------------

    @Override
    protected void putVariables(Map<String, String> variables) {
        variables.put(ScopeFormat.SCOPE_HOST, hostname);
        variables.put(ScopeFormat.SCOPE_JOB_NAME, jobName);
    }

    @Override
    protected Iterable<? extends ComponentMetricGroup> subComponents() {
        return Collections.EMPTY_LIST;
    }

    @Override
    protected String getGroupName(CharacterFilter filter) {
        return "JlogstashJob";
    }
}

