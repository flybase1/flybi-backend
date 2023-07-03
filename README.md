# 飞云 BI

区别于传统BI，用户只需要导入原始数据集、并输入分析诉求，就能自动生成可视化图表及分析结论，实现数据分析的降本增效。

### 主流框架

- Spring Boot 2.7.4
- Spring MVC
- MyBatis + MyBatis Plus 数据访问（开启分页）
- Spring Boot 调试工具和项目处理器
- Spring AOP 切面编程
- Spring Scheduler 定时任务
- Spring 事务注解
- Redisson 进行限流处理
- 多线程异步化
- Rabbit MQ 消息队列

### 工具类

- Easy Excel 表格处理
- Hutool 工具库
- Gson 解析库
- Apache Commons Lang3 工具类
- Lombok 注解

### 业务特性

- Spring Session Redis 分布式登录
- 全局请求响应拦截器（记录日志）
- 全局异常处理器
- 自定义错误码
- 封装通用响应类
- Swagger + Knife4j 接口文档
- 自定义权限注解 + 全局校验
- 全局跨域处理
- 长整数丢失精度解决
- 多环境配置


## 改进以及业务流程
基础流程
![img.png](img.png)
![img_5.png](img_5.png)
缺点: 等待AI服务调用生成最后结果后才返回，业务请求过多，服务器宕机

改进
采用异步化
![img_2.png](img_2.png)
缺点：单机部署

进阶流程
采用Rabbit MQ
![img_1.png](img_1.png)
![img_4.png](img_4.png)
![img_6.png](img_6.png)

用户不需要进行等待，





