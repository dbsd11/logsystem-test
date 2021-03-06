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

package group.bison.test.logsystem.parse.jlogstash.core.utils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * company: www.dtstack.com
 * author: toutian
 * create: 2017/5/15
 */
public class Base64Util {

    /**
     * base 加密
     *
     * @param text
     * @return
     * @author toutian
     */
    public static String baseEncode(String text) {
        return new String(Base64.getEncoder().encode(text.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }

    /**
     * base 解码
     *
     * @param encode
     * @return
     * @author toutian
     */
    public static String baseDecode(String encode) {
        return new String(Base64.getDecoder().decode(encode.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    }


    public static byte[] baseEncode(byte[] encode) {
        return Base64.getEncoder().encode(encode);
    }

    public static byte[] baseDecode(byte[] encode) {
        return Base64.getDecoder().decode(encode);
    }

}
