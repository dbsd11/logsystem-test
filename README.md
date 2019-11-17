jlogstash
===========================
logstash的java实现，可以自己写日志解析、输入和输出了。项目借鉴**GITHUB**代码库：[jlogstash](https://github.com/DTStack/jlogstash),[hangout](https://github.com/childe/hangout)

---
## 为什么要自己写logstash 
       项目有日志采集需求，比较流行的方式是通过flume+kafka+logstash的方式实现日志的解析和输出，但是这样就提高了项目维护和部署的复杂程度。
       如果可以使用logstash的开源实现并整合到已有服务中，那么就可以不依赖服务器环境，自身做到服务日志的采集、解析和输出，实现服务的WORA。很幸运，GITHUB上有这样的项目，还不止一个。

## 适用场景
    对日志相关服务组件环境要求较低，但是日志功能强依赖项目，例如需要在管理页面查询系统日志、操作日志等。部署简单，开箱即用。
    
## 已实现功能
```
1. 可独立启动运行(jar -jar xxxx.jar)，开箱即用
2. 可嵌入服务，以主进程/fork子进程两种方式启动
3. jlogstash fork进程的监听，修改配置可触发自动重启
```

## 样例
以kafka输入->filter解析->jdbc输出为例，配置文件**example.yml**如下：
```java
inputs:
    - Kafka10:
        codec: json
        encoding: UTF8 # defaut UTF8
        topic:
          some-topic
        groupId:
          som-groupId
        bootstrapServers:
          0.0.0.0：9092

        consumerSettings:
          group.id: some-groupId
          auto.commit.interval.ms: "1000"

filters:
   - Add:
        fields: {"ip":"%{ip}%"}

outputs:
   - Jdbc:
        driverJarPath: ''
        driverClass: 'com.mysql.jdbc.Driver'
        connectionString: 'jdbc:mysql://0.0.0.0:3306/test1?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull'
        statement:
            - 'insert into testi(id) values("?") '
            - '%{timeStamp}'
```
## notes
```
1. 支持插件化配置，已实现JARClassLoader去加载相应jar包，暂不支持卸载
2. 以fork进程方式启动，对已有服务性能影响较小(只需要内存和cpu够用就行)
3. 配置上支持yaml和json两个格式，如有自定义input/filter/output可自行添加相应配置
```
## TODO
插件加载等性能优化、日志链路跟踪、分布式实现等等，大家一起来摸索啊...