package group.bison.test.logsystem.collect.parse.impl;

import com.alibaba.fastjson.JSON;
import group.bison.test.logsystem.collect.LoggerNames;
import group.bison.test.logsystem.collect.config.DissectParserConfig;
import group.bison.test.logsystem.collect.parse.ILogParser;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.dissect.DissectParser;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Created by diaobisong on 2019/11/11.
 */
public class CommonLogParser implements ILogParser {

    private static Map<String, CommonLogParser> logParserMap = new ConcurrentHashMap<>(16);

    private String loggerName;

    private List<DissectParser> dissectParserList;

    public CommonLogParser(String loggerName) {
        this.loggerName = loggerName;

        String pattern = DissectParserConfig.LogPatternProvider.getInstance().getPattern(loggerName);
        if (StringUtils.isNotEmpty(pattern)) {
            List<String> subPatternList = JSON.parseArray(pattern, String.class);
            List<DissectParser> dissectParserList = subPatternList.stream().map(subPattern -> new DissectParser(subPattern, null)).collect(Collectors.toList());
            this.dissectParserList = dissectParserList;
        }
    }

    @Override
    public String getLoggerName() {
        return loggerName;
    }

    @Override
    public String parse(String level, String callerClass, String logTimeStr, String message, String formattedMessage) {
        if (CollectionUtils.isEmpty(dissectParserList)) {
            return message;
        }

        //拼接大量" "用于解析pattern最后的默认值
        for (int i = 0; i < 50; i++) {
            formattedMessage = String.join(" ", formattedMessage, "");
        }

        Map<String, String> parseParamMap = null;
        for (DissectParser dissectParser : dissectParserList) {
            try {
                Map<String, String> unmodifyParseParamMap = dissectParser.parse(formattedMessage);
                parseParamMap = new HashMap<>(unmodifyParseParamMap);
                break;
            } catch (Exception e) {
            }
        }

        if (CollectionUtils.isEmpty(parseParamMap)) {
            return message;
        }

        //填补默认值
        String key = null;
        String value = null;
        String[] keySpliteArray = null;
        for (Map.Entry<String, String> parseParamEntry : new HashMap<String, String>(parseParamMap).entrySet()) {
            key = parseParamEntry.getKey().trim();
            value = parseParamEntry.getValue().trim();
            if (key.contains(":")) {
                keySpliteArray = key.split(":");
                key = keySpliteArray[0];
                value = keySpliteArray[1];
            }
            parseParamMap.put(key, value);
        }

        if (loggerName.equalsIgnoreCase(LoggerNames.SystemLoggerName.class.getName())) {
            parseParamMap.put("logLevel", level);
            if (!parseParamMap.containsKey("logOrigin")) {
                parseParamMap.put("logOrigin", "自动采集");
            }
            if (!parseParamMap.containsKey("createTime")) {
                parseParamMap.put("createTime", logTimeStr);
            }
        }

        return JSON.toJSONString(parseParamMap);
    }

    public static CommonLogParser getInstance(String loggerName) {
        if (!logParserMap.containsKey(loggerName)) {
            CommonLogParser commonLogParser = new CommonLogParser(loggerName);
            if (CollectionUtils.isEmpty(commonLogParser.dissectParserList)) {
                return commonLogParser;
            } else {
                logParserMap.putIfAbsent(loggerName, commonLogParser);
            }
        }
        return logParserMap.get(loggerName);
    }

    public static void main(String[] args) {
        DissectParser dissectParser = new DissectParser("调用三方接口%{} sourceType:%{sourceType} callerSystem:%{callerSystem->} apiName:%{apiName} inParam:%{inParam->} outParam:%{outParam->} respCt:%{respCt} isSuccess:%{isSuccess} hasResponse:%{hasResponse} %{?suffix}", null);
        Map<String, String> parseMap = dissectParser.parse("调用三方接口 sourceType:asdas callerSystem:1231 12321 apiName:testApi inParam:12345 6 outParam:654 321 respCt:11111 isSuccess:true hasResponse:false    ");
        parseMap = dissectParser.parse("app Application stoped");
        int i = 0;
    }
}
