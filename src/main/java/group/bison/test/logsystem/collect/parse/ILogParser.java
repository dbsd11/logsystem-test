package group.bison.test.logsystem.collect.parse;

/**
 * 日志解析接口
 * Created by diaobisong on 2019/11/11.
 */
public interface ILogParser {

    String getLoggerName();

    String parse(String level, String callerClass, String logTimeStr, String message, String formattedMessage);
}
