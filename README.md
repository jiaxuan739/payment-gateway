环境准备	Docker 启动 MySQL/Redis/RabbitMQ

启动应用	Maven Wrapper 启动 Spring Boot

日常操作	支付创建、查询、退款、手动对账的 curl 命令

状态码速查	订单状态 + 响应码对照表

对账差异类型	MATCH / LOCAL_ONLY / CHANNEL_ONLY / AMOUNT_MISMATCH 的处理建议

故障排查	基础设施验证命令、日志查看、管理界面地址

停止服务	正常停止 vs 清空数据

定时任务	每日凌晨 2 点对账 + 告警



---------------------------------------------------------------------
payment-gateway/
├── pom.xml                          # Spring Boot 3.2 + MyBatis-Plus + Redis + RabbitMQ

├── docker-compose.yml               # MySQL 8.0 + Redis 7 + RabbitMQ 3.12

├── Dockerfile

├── mvnw.cmd + .mvn/wrapper/         # Maven Wrapper（无需安装 Maven）

└── src/main/

    ├── java/com/payment/gateway/
    
    │   ├── GatewayApplication.java
    
    │   ├── channel/                 # 渠道策略：接口 + 3 模拟实现
    
    │   │   ├── PaymentChannel.java
    
    │   │   ├── WechatPayChannel.java    # ~98% 成功率, 200-400ms, 0.60% 费率
    
    │   │   ├── AlipayChannel.java       # ~97% 成功率, 150-350ms, 0.55% 费率
    
    │   │   └── UnionPayChannel.java     # ~96% 成功率, 250-550ms, 0.50% 费率
    
    │   ├── service/
    
    │   │   ├── PaymentService.java      # 支付主流程 + 故障自愈降级
    
    │   │   ├── RoutingService.java      # 智能路由引擎（加权评分算法）
    
    │   │   ├── ChannelMetricsService.java # Redis 实时指标（EMA 算法）
    
    │   │   └── ReconciliationService.java # 异步对账系统
    
    │   ├── controller/
    
    │   │   ├── PaymentController.java   # /api/payment/create|query|refund
    
    │   │   └── ReconciliationController.java # /api/reconciliation/trigger|errors
    
    │   ├── model/                   # MyBatis-Plus 实体
    
    │   ├── dto/                     # DTO + Builder 模式
    
    │   ├── repository/              # MyBatis Mapper
    
    │   ├── enums/                   # PayChannel/PayScene/OrderStatus
    
    │   └── config/                  # Redis/RabbitMQ/Scheduling 配置
    
    └── resources/
    
        ├── application.yml
        
        └── db/schema.sql             # 建表 DDL + 渠道费率初始数据
