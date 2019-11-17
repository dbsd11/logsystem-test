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
package group.bison.test.logsystem.parse.jlogstash.core.factory;

import java.lang.reflect.Constructor;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import group.bison.test.logsystem.parse.jlogstash.core.callback.ClassLoaderCallBackMethod;
import group.bison.test.logsystem.parse.jlogstash.core.filters.BaseFilter;
import group.bison.test.logsystem.parse.jlogstash.core.filters.FilterProxy;
import group.bison.test.logsystem.parse.jlogstash.core.filters.IBaseFilter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:26:24
 * Company: www.dtstack.com
 *
 * @author sishu.yss
 */
public class FilterFactory extends InstanceFactory {

    private final static String PLUGINTYPE = "filter";

    @SuppressWarnings("rawtypes")
    private static IBaseFilter getInstance(String filterType, Map filterConfig) throws Exception {
        ClassLoader classLoader = getClassLoader(filterType, PLUGINTYPE);
        BaseFilter filterInstance = ClassLoaderCallBackMethod.callbackAndReset(() -> {
            Class<?> filterClass = classLoader.loadClass(getClassName(filterType, PLUGINTYPE));
            //设置static field
            configInstance(filterClass, filterConfig);
            Constructor<?> ctor = filterClass.getConstructor(Map.class);
            return (BaseFilter) ctor.newInstance(filterConfig);
        }, classLoader, true);
        //设置非static field
        configInstance(filterInstance, filterConfig);
        IBaseFilter baseFilter = new FilterProxy(filterInstance);
        baseFilter.prepare();
        return baseFilter;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static List<IBaseFilter> getBatchInstance(List<Map> filters) throws Exception {
        if (filters == null || filters.size() == 0) {
            return null;
        }
        List<IBaseFilter> baseFilters = Lists.newArrayList();
        for (int i = 0; i < filters.size(); i++) {
            Iterator<Entry<String, Map>> filterIT = filters.get(i).entrySet().iterator();
            while (filterIT.hasNext()) {
                Entry<String, Map> filterEntry = filterIT.next();
                String filterType = filterEntry.getKey();
                Map filterConfig = filterEntry.getValue();
                if (filterConfig == null) {
                    filterConfig = Maps.newLinkedHashMap();
                }
                IBaseFilter baseFilter = getInstance(filterType, filterConfig);
                baseFilters.add(baseFilter);
            }
        }
        return baseFilters;
    }
}
