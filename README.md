<br>
- 该项目基于aop-logging<br>
添加了标记客户端请求id：%X{ReqId} 需要设置filter<br>
方法执行时长：%X{elapsedTime}<br>
业务模块标识: %X{bizModule} 需要设置@LogModule<br>
当前登录用户: %X{userId} 需要设置filter并实现userId获取接口<br>
请求类： %X{callingClass}<br>
请求方法: %X{callingMethod}<br>


**引用jar**
````
        <dependency>
            <groupId>com.github.jeffreyning</groupId>
            <artifactId>nh-aop-logging</artifactId>
            <version>1.0.0</version>
        </dependency>
````

**springboot工程中还应确保直接或间接引入了**
````
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-aop</artifactId>
        </dependency>
````

**如果需要按照json输出日志还应引入**
```` 
        <dependency>
            <groupId>net.logstash.logback</groupId>
            <artifactId>logstash-logback-encoder</artifactId>
            <version>5.0</version>
        </dependency>               
````      

**初始化日志aop**
````
@Configuration
@EnableAspectJAutoProxy
public class LoggerConfig {

    private static final boolean SKIP_NULL_FIELDS = true;
    private static final int CROP_THRESHOLD = 7;
    private static final Set<String> EXCLUDE_SECURE_FIELD_NAMES = Collections.<String>emptySet();

    @Bean
    public AOPLogger getLoggerBean() {
        AOPLogger aopLogger = new AOPLogger();
        aopLogger.setLogAdapter(new UniversalLogAdapter(SKIP_NULL_FIELDS, CROP_THRESHOLD, EXCLUDE_SECURE_FIELD_NAMES));
        return aopLogger;
    }
}
````

**初始化filter**
注入自定义获取userId的实现类，支持ReqId和userId的获取
````
    @Bean
    public FilterRegistrationBean getDemoFilter() {
        ReqIdFilter reqIdFilter = new ReqIdFilter();
        reqIdFilter.setUserInfoLog(new UserInfoLog() {
            @Override
            public String getUserId() {
                //自定义获取userid的逻辑
                return "admin";
            }
        });
        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
        registrationBean.setFilter(reqIdFilter);
        List<String> urlPatterns = new ArrayList<String>();
        urlPatterns.add("/*");
        registrationBean.setUrlPatterns(urlPatterns);
        registrationBean.setOrder(100);
        return registrationBean;
    }
````

**在controller类上写入注解也可以写在方法上**
@LogInfo 记录接口info日志
@LogException 记录接口异常日志
@LogModule 设置业务模块标识

````
@RestController
@RequestMapping("/test")
@LogInfo
@LogException
@LogModule("system")
public class TestCtl {

    @GetMapping("/query")
    public Map query(String echo){
        Map retMap=new HashMap();
        retMap.put("status",0);
        retMap.put("msg", "success");
        retMap.put("data", echo);
        return retMap;
    }

    @GetMapping("/createException")
    public Map createException(String echo){
        Map retMap=null;
        retMap.put("status",0);
        retMap.put("msg", "success");
        retMap.put("data", echo);
        return retMap;
    }
}
````      

日志格式配置
````
	<appender name="interfaceConsole" class="ch.qos.logback.core.ConsoleAppender">
		<encoder>
			<pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} [%file:%line]- class=%X{callingClass} method=%X{callingMethod} reqId=%X{reqId} userId=%X{userId} bizModule=%X{bizModule}  elapsedTime=%X{elapsedTime} - %msg%n</pattern>
			<charset>UTF-8</charset>
		</encoder>
	</appender>
````
	
按照json格式输出，方便elk收集并分析接口日志
````
	<appender name="interfaceLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
		<file>${logFile}.interface</file>
		<encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
			<providers>
				<pattern>
					<pattern>
						{
						"project": "log-demo",
						"timestamp": "%date{\"yyyy-MM-dd'T'HH:mm:ss,SSSZ\"}",
						"log_level": "%level",
						"thread": "%thread",
						"class_name": "%X{callingClass}",
						"class_method":"%X{callingMethod}",
						"message": "%message",
						"req_id": "%X{reqId}",
						"user_id": "%X{userId}",
						"biz_module": "%X{bizModule}",
                        "elapsedTime": "%X{elapsedTime}",
						"stack_trace": "%exception{5}"
						}
					</pattern>
				</pattern>
			</providers>
		</encoder>
		<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
			<level>INFO</level>
		</filter>
		<rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
			<fileNamePattern>${logFile}.interface.%d{yyyy-MM-dd}.%i</fileNamePattern>
			<maxFileSize>${maxFileSize}</maxFileSize>
			<maxHistory>${maxHistory}</maxHistory>
		</rollingPolicy>
	</appender>
````

**配置内置loger与appender绑定**
````
	<logger name="com.github.nickvl.xspring.core.log.aop.AOPLogger" level="INFO" additivity="false" >
		<appender-ref ref="interfaceLog"/>
		<appender-ref ref="interfaceConsole"/>
	</logger>
````

**输出效果**
````
2020-10-20 19:00:21.398 [http-nio-8080-exec-1] INFO  c.g.n.xspring.core.log.aop.AOPLogger [LogStrategy.java:184]- class=com.github.jeffreyning.demo.controller.TestCtl method=query reqId=5fbce7c52847c3398cb1c9d5 userId=admin bizModule=system  elapsedTime= - calling: query(echo=111)
2020-10-20 19:00:21.429 [http-nio-8080-exec-1] INFO  c.g.n.xspring.core.log.aop.AOPLogger [LogStrategy.java:189]- class=com.github.jeffreyning.demo.controller.TestCtl method=query reqId=5fbce7c52847c3398cb1c9d5 userId=admin bizModule=system  elapsedTime=31 - returning: query(1 arguments):HashMap[{msg=success,data=111,status=0}]
````

**异常日志输出效果**
````
2020-11-24 19:45:27.453 [http-nio-8080-exec-3] INFO  c.g.n.xspring.core.log.aop.AOPLogger [LogStrategy.java:184]- class=com.github.jeffreyning.demo.controller.TestCtl method=createException reqId=5fbcf2576d198936a07e718d userId=admin bizModule=system elapsedTime= - calling: createException(echo=NIL)
2020-11-24 19:45:27.457 [http-nio-8080-exec-3] ERROR c.g.n.xspring.core.log.aop.AOPLogger [LogStrategy.java:125]- class=com.github.jeffreyning.demo.controller.TestCtl method=createException reqId=5fbcf2576d198936a07e718d userId=admin bizModule=system elapsedTime=0 - throwing: createException(1 arguments):class java.lang.NullPointerException
````

**json日志输出效果**
````
{"project":"log-demo","timestamp":"2020-11-24T19:40:57,433+0800","log_level":"INFO","thread":"http-nio-8080-exec-1","class_name":"com.github.jeffreyning.demo.controller.TestCtl","class_method":"query","line_number":"184","message":"calling: query(echo=111)","req_id":"5fbcf1496d198936a07e718c","user_id":"admin","biz_module":"system","elapsedTime":"","stack_trace":""}
{"project":"log-demo","timestamp":"2020-11-24T19:40:57,473+0800","log_level":"INFO","thread":"http-nio-8080-exec-1","class_name":"com.github.jeffreyning.demo.controller.TestCtl","class_method":"query","line_number":"189","message":"returning: query(1 arguments):HashMap[{msg=success,data=111,status=0}]","req_id":"5fbcf1496d198936a07e718c","user_id":"admin","biz_module":"system","elapsedTime":"24","stack_trace":""}
````
