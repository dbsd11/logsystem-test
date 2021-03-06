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
package group.bison.test.logsystem.parse.jlogstash.core.utils;

import group.bison.test.logsystem.parse.jlogstash.core.property.SystemProperty;
import org.apache.commons.lang3.StringUtils;


/**
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:28:43
 * Company: www.dtstack.com
 *
 * @author sishu.yss
 */
public class Package {
    private static String point = ".";

    public static String getRealClassName(String name, String key) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        if (name.indexOf(point) >= 0) {
            return name;
        }
        return String.join("", SystemProperty.getSystemProperty(key), point, name.toLowerCase(), point, name);
    }
}
