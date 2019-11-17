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
package group.bison.test.logsystem.parse.jlogstash.pipeline.filters.split;

import group.bison.test.logsystem.parse.jlogstash.core.filters.BaseFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


/**
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:54:01
 * Company: www.dtstack.com
 *
 * @author sishu.yss
 */
public class Split extends BaseFilter {
    private static final Logger logger = LoggerFactory.getLogger(Split.class.getName());

    public String source = "message";
    public String separator;
    public String fields;

    private List<String> splitField;

    @SuppressWarnings("rawtypes")
    public Split(Map config) {
        super(config);
    }

    @SuppressWarnings("unchecked")
    public void prepare() {

        splitField = Arrays.asList(fields.split(","));

    }

    ;

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    protected Map filter(Map event) {
        if (!event.containsKey(this.source)) {
            return event;
        }
        parseSplit(event, event.get(this.source).toString(), separator, splitField);
        return event;
    }

    public boolean parseSplit(Map<String, Object> event, String value, String splitChar, List<String> splitField) {
        try {
            int size = splitField.size();
            if (value != null && !"".equals(value) && splitField != null && size != 0) {
                String[] vs = value.split(splitChar);
                if (vs.length >= 1) {
                    for (int i = 0; i < vs.length; i++) {
                        if (i < size) {
                            event.put(splitField.get(i).trim(), vs[i].trim());
                        }
                    }
                } else
                    return false;
            }
        } catch (Exception e) {
            logger.error("parseSplit error", e);
            return false;
        }
        return true;
    }

}
