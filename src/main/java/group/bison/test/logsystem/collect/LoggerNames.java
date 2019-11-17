package group.bison.test.logsystem.collect;

/**
 * 预定义的Logger，使用方式: Logger LOGGER = LoggerFactory.getLogger(LoggerNames.SystemLoggerName.class);
 * 用于配置支持写入本地文件的Logger，否则不支持本地落盘(例如其他业务日志)
 * <p>
 * Created by diaobisong on 2019/11/5.
 */
public class LoggerNames {

    /**
     * 系统日志
     */
    public static class SystemLoggerName {
    }

    /**
     * 用户日志
     */
    public static class UserOperationLoggerName {
    }

    /**
     * 脱敏日志
     */
    public static class DesensiteLoggerName {
    }
}
