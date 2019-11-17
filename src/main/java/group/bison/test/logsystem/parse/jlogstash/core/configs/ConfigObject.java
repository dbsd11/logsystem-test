/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package group.bison.test.logsystem.parse.jlogstash.core.configs;

import java.util.Map;
import java.util.List;
/**
 * Created by sishu.yss on 2018/8/23.
 */
public class ConfigObject {

    private List<Map> inputs;

    private List<Map> filters;

    private List<Map> outputs;

    private List<Map> metrics;

    public List<Map> getInputs() {
        return inputs;
    }

    public void setInputs(List<Map> inputs) {
        this.inputs = inputs;
    }

    public List<Map> getFilters() {
        return filters;
    }

    public void setFilters(List<Map> filters) {
        this.filters = filters;
    }

    public List<Map> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Map> outputs) {
        this.outputs = outputs;
    }

    public List<Map> getMetrics() {
        return metrics;
    }

    public void setMetrics(List<Map> metrics) {
        this.metrics = metrics;
    }
}
