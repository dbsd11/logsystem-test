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

package group.bison.test.logsystem.parse.jlogstash.core.metrics.scope;

import group.bison.test.logsystem.parse.jlogstash.core.metrics.base.CharacterFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static group.bison.test.logsystem.parse.jlogstash.core.metrics.util.Preconditions.checkNotNull;

/**
 * copy from https://github.com/apache/flink
 *
 */
public abstract class ScopeFormat {

	private static CharacterFilter defaultFilter = new CharacterFilter() {
		@Override
		public String filterCharacters(String input) {
			return input;
		}
	};

	// ------------------------------------------------------------------------
	//  Scope Format Special Characters
	// ------------------------------------------------------------------------

	/**
	 * If the scope format starts with this character, then the parent components scope
	 * format will be used as a prefix.
	 *
	 */
	public static final String SCOPE_INHERIT_PARENT = "*";

	public static final String SCOPE_SEPARATOR = ".";

	private static final String SCOPE_VARIABLE_PREFIX = "<";
	private static final String SCOPE_VARIABLE_SUFFIX = ">";

	// ------------------------------------------------------------------------
	//  Scope Variables
	// ------------------------------------------------------------------------

	public static final String SCOPE_HOST = asVariable("host");

	public static final String SCOPE_PLUGINE_TYPE = asVariable("plugin_type");

	public static final String SCOPE_PLUGINE_NAME = asVariable("plugin_name");

	public static final String SCOPE_JOB_NAME = asVariable("job_name");

	// ------------------------------------------------------------------------
	//  Scope Format Base
	// ------------------------------------------------------------------------

	/** The scope format. */
	private final String format;

	/** The format, split into components. */
	private final String[] template;

	private final int[] templatePos;

	private final int[] valuePos;

	// ------------------------------------------------------------------------

	protected ScopeFormat(String format, ScopeFormat parent, String[] variables) {
		checkNotNull(format, "format is null");

		final String[] rawComponents = format.split("\\" + SCOPE_SEPARATOR);

		// compute the template array
		final boolean parentAsPrefix = rawComponents.length > 0 && rawComponents[0].equals(SCOPE_INHERIT_PARENT);
		if (parentAsPrefix) {
			if (parent == null) {
				throw new IllegalArgumentException("Component scope format requires parent prefix (starts with '"
					+ SCOPE_INHERIT_PARENT + "'), but this component has no parent (is root component).");
			}

			this.format = format.length() > 2 ? format.substring(2) : "<empty>";

			String[] parentTemplate = parent.template;
			int parentLen = parentTemplate.length;

			this.template = new String[parentLen + rawComponents.length - 1];
			System.arraycopy(parentTemplate, 0, this.template, 0, parentLen);
			System.arraycopy(rawComponents, 1, this.template, parentLen, rawComponents.length - 1);
		}
		else {
			this.format = format.isEmpty() ? "<empty>" : format;
			this.template = rawComponents;
		}

		// --- compute the replacement matrix ---
		// a bit of clumsy Java collections code ;-)

		HashMap<String, Integer> varToValuePos = arrayToMap(variables);
		List<Integer> templatePos = new ArrayList<>();
		List<Integer> valuePos = new ArrayList<>();

		for (int i = 0; i < template.length; i++) {
			final String component = template[i];

			// check if that is a variable
			if (component != null && component.length() >= 3 &&
					component.charAt(0) == '<' && component.charAt(component.length() - 1) == '>') {

				// this is a variable
				Integer replacementPos = varToValuePos.get(component);
				if (replacementPos != null) {
					templatePos.add(i);
					valuePos.add(replacementPos);
				}
			}
		}

		this.templatePos = integerListToArray(templatePos);
		this.valuePos = integerListToArray(valuePos);
	}

	// ------------------------------------------------------------------------

	public String format() {
		return format;
	}

	protected final String[] copyTemplate() {
		String[] copy = new String[template.length];
		System.arraycopy(template, 0, copy, 0, template.length);
		return copy;
	}

	protected final String[] bindVariables(String[] template, String[] values) {
		final int len = templatePos.length;
		for (int i = 0; i < len; i++) {
			template[templatePos[i]] = values[valuePos[i]];
		}
		return template;
	}

	// ------------------------------------------------------------------------

	@Override
	public String toString() {
		return "ScopeFormat '" + format + '\'';
	}

	// ------------------------------------------------------------------------
	//  Utilities
	// ------------------------------------------------------------------------

	/**
	 * Formats the given string to resemble a scope variable.
	 *
	 * @param scope The string to format
	 * @return The formatted string
	 */
	public static String asVariable(String scope) {
		return SCOPE_VARIABLE_PREFIX + scope + SCOPE_VARIABLE_SUFFIX;
	}

	public static String concat(String... components) {
		return concat(defaultFilter, '.', components);
	}

	public static String concat(CharacterFilter filter, String... components) {
		return concat(filter, '.', components);
	}

	public static String concat(Character delimiter, String... components) {
		return concat(defaultFilter, delimiter, components);
	}

	/**
	 * Concatenates the given component names separated by the delimiter character. Additionally
	 * the character filter is applied to all component names.
	 *
	 * @param filter Character filter to be applied to the component names
	 * @param delimiter Delimiter to separate component names
	 * @param components Array of component names
	 * @return The concatenated component name
	 */
	public static String concat(CharacterFilter filter, Character delimiter, String... components) {
		StringBuilder sb = new StringBuilder();
		sb.append(filter.filterCharacters(components[0]));
		for (int x = 1; x < components.length; x++) {
			sb.append(delimiter);
			sb.append(filter.filterCharacters(components[x]));
		}
		return sb.toString();
	}

	protected static String valueOrNull(Object value) {
		return (value == null || (value instanceof String && ((String) value).isEmpty())) ?
				"null" : value.toString();
	}

	protected static HashMap<String, Integer> arrayToMap(String[] array) {
		HashMap<String, Integer> map = new HashMap<>(array.length);
		for (int i = 0; i < array.length; i++) {
			map.put(array[i], i);
		}
		return map;
	}

	private static int[] integerListToArray(List<Integer> list) {
		int[] array = new int[list.size()];
		int pos = 0;
		for (Integer i : list) {
			array[pos++] = i;
		}
		return array;
	}
}
