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
package group.bison.test.logsystem.parse.jlogstash.pipeline.filters.trim;

import java.util.List;
import java.util.Map;

import group.bison.test.logsystem.parse.jlogstash.core.annotation.Required;
import group.bison.test.logsystem.parse.jlogstash.core.filters.BaseFilter;


/**
 * 
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:55:04
 * Company: www.dtstack.com
 * @author sishu.yss
 *
 */
public class Trim extends BaseFilter {
	public Trim(Map config) {
		super(config);
	}

	@Required(required=true)
	private static  List<String> fields;

	public void prepare() {
	}

	@Override
	protected Map filter(final Map event) {
		for (String field : fields) {
			if (event.containsKey(field)) {
				event.put(field, ((String) event.remove(field)).trim());
			}
		}
		return event;
	}
}
