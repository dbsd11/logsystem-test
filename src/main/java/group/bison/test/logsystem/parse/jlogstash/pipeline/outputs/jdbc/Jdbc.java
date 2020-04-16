package group.bison.test.logsystem.parse.jlogstash.pipeline.outputs.jdbc;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.util.StringUtils;
import group.bison.test.logsystem.parse.jlogstash.core.annotation.Required;
import group.bison.test.logsystem.parse.jlogstash.core.classloader.JARClassLoader;
import group.bison.test.logsystem.parse.jlogstash.core.outputs.BaseOutput;
import group.bison.test.logsystem.parse.jlogstash.core.render.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by diaobisong on 2019/11/7.
 */
public class Jdbc extends BaseOutput {
    private Logger LOG = LoggerFactory.getLogger(Jdbc.class);

    @Required(required = false)
    private static String driverJarPath;

    @Required(required = true)
    private static String driverClass;

    @Required(required = true)
    private static String connectionString;

    @Required(required = true)
    private static List<String> statement;

    private static URLClassLoader urlClassLoader;

    private static DataSource dataSource;

    private static AtomicBoolean prepared = new AtomicBoolean(false);

    public Jdbc(Map config) {
        super(config);
    }

    @Override
    protected void emit(Map event) {
        try {
            String sql = statement.get(0);
            List<String> paramStrList = statement.subList(1, statement.size());
            for (String paramStr : paramStrList) {
                sql = sql.replaceFirst("\\?", Formatter.format(event, paramStr));
            }

            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            boolean success = statement.execute(sql);
            if (!success) {
                LOG.error("output emit failed, sql:{}", sql);
            } else if (LOG.isInfoEnabled()) {
                LOG.info("output emit success, event:{}", event);
            }
        } catch (Exception e) {
            LOG.error("output emit failed:", e.getMessage());
        }
    }

    @Override
    public void prepare() {
        if (prepared.get()) {
            return;
        }

        if (!StringUtils.isEmpty(driverJarPath)) {
            try {
                URLClassLoader jarClassLoader = new JARClassLoader(new URL[]{new File(driverJarPath).toURI().toURL()}, Class.class.getClassLoader());
                jarClassLoader.loadClass(driverClass);
                this.urlClassLoader = jarClassLoader;
            } catch (Exception e) {
                LOG.error("output prepare failed:{}", e.getMessage());
            }
        }

        DruidDataSource druidDataSource = new DruidDataSource();
        druidDataSource.setDbType("mysql");
        druidDataSource.setDriverClassLoader(urlClassLoader);
        druidDataSource.setDriverClassName(driverClass);

        Properties connectionProperties = getConnectionProperties(connectionString);
        druidDataSource.setUrl(connectionString);
        druidDataSource.setUsername(connectionProperties.getProperty("user"));
        druidDataSource.setPassword(connectionProperties.getProperty("password"));
        druidDataSource.setConnectionInitSqls(Collections.singletonList("SELECT 1"));
        druidDataSource.setConnectProperties(connectionProperties);

		druidDataSource.setTestOnBorrow(false);
		druidDataSource.setTestOnReturn(false);
		druidDataSource.setTestWhileIdle(true);
		druidDataSource.setTimeBetweenEvictionRunsMillis(600000);
		druidDataSource.setMaxWait(30000L);
		druidDataSource.setMaxActive(300);
		druidDataSource.setUseUnfairLock(true);
		druidDataSource.setKeepAlive(true);
		druidDataSource.setEnable(true);
		druidDataSource.setInitialSize(4);
		druidDataSource.setAsyncInit(true);

		druidDataSource.setUseGlobalDataSourceStat(true);
		druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(20);
        
        try {
            druidDataSource.init();
            prepared.set(true);
        } catch (Exception e) {
            LOG.error("output prepare druidDataSource init failed:{}", e.getMessage());
        }
        this.dataSource = druidDataSource;
    }

    @Override
    public void release() {
        try {
            if (dataSource instanceof DruidDataSource) {
                ((DruidDataSource) dataSource).close();
            }

            urlClassLoader.close();
            urlClassLoader = null;
        } catch (Exception e) {
            LOG.error("output release failed:{}", e.getMessage());
        } finally {
            prepared.set(false);
        }
    }

    Properties getConnectionProperties(String connectionString) {
        String propertyStr = connectionString.contains("?") ? connectionString.substring(connectionString.indexOf("?") + 1) : null;
        if (StringUtils.isEmpty(propertyStr)) {
            return new Properties();
        }

        Properties properties = new Properties();

        String[] propertyKVArray = propertyStr.split("&");
        for (String propertyKV : propertyKVArray) {
            String key = propertyKV.contains("=") ? propertyKV.substring(0, propertyKV.indexOf("=")) : propertyKV;
            String value = propertyKV.contains("=") ? propertyKV.substring(propertyKV.indexOf("=") + 1) : null;
            if (!StringUtils.isEmpty(value)) {
                properties.put(key, value);
            }
        }
        return properties;
    }
}
