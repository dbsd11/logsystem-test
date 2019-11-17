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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;

import group.bison.test.logsystem.parse.jlogstash.core.exception.LogstashException;
import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;
import org.codehaus.jackson.JsonParser.Feature;
import sun.misc.BASE64Decoder;


/**
 * 
 * Reason: TODO ADD REASON(可选)
 * Date: 2016年8月31日 下午1:25:45
 * Company: www.dtstack.com
 * @author sishu.yss
 *
 */
@SuppressWarnings("rawtypes")
public class YamlConfig implements Config{
    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static Logger logger = LoggerFactory.getLogger(YamlConfig.class);
    private static ObjectMapper objectMapper = new ObjectMapper();
    private static final BASE64Decoder decoder = new BASE64Decoder();
    static {
        objectMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);//设置可用单引号
        objectMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);//设置字段可以不
    }

    @Override
    public ConfigObject parse(String configFile) throws Exception{

        logger.info("jlogstash config of the configFile:{}", configFile);

//        String conf = Base64Util.baseDecode(GZipUtil.deCompress(configFile));
        String conf = URLDecoder.decode(configFile, "UTF-8");
        logger.info("conf:{}", conf);

        ConfigObject configObject = null;
        if(conf.startsWith("{")&&conf.endsWith("}")){
            configObject =  objectMapper.readValue(conf,ConfigObject.class);
        }else{
            Yaml yaml = new Yaml();
            if (conf.startsWith(YamlConfig.HTTP) || conf.startsWith(YamlConfig.HTTPS)) {
                URL httpUrl;
                URLConnection connection;
                httpUrl = new URL(conf);
                connection = httpUrl.openConnection();
                connection.connect();
                configObject =  yaml.loadAs(connection.getInputStream(),ConfigObject.class);
            } else {
                FileInputStream input = new FileInputStream(new File(conf));
                configObject = yaml.loadAs(input,ConfigObject.class);
            }
        }

        if(configObject == null){
            throw new LogstashException("conf is error...");
        }
        return configObject;
    }
}
