# Payment Gateway SOP (Standard Operating Procedure)

## 1. 环境准备

### 前置条件
- Java 17+
- Docker Desktop（运行中）

### 启动基础设施（首次或重启后）
```bash
cd payment-gateway
docker-compose up -d
```
验证：
```bash
docker ps --filter "name=payment-" --format "table {{.Names}}\t{{.Status}}"
```
应看到 3 个容器：payment-mysql、payment-redis、payment-rabbitmq 均为 `Up`。

---

## 2. 启动应用

```bash
cd payment-gateway
./mvnw.cmd spring-boot:run
```
验证：
```bash
curl http://localhost:8080/actuator/health
```

---

## 3. 日常操作

### 3.1 创建支付订单
```bash
curl -X POST http://localhost:8080/api/payment/create \
  -H "Content-Type: application/json" \
  -d '{
    "orderNo": "ORD20260718001",
    "amount": 100.00,
    "scene": "NATIVE",
    "subject": "商品名称"
  }'
```
- `scene`：NATIVE（扫码）/ H5（手机网页）/ APP（App内）
- `channel`：可选，不传则自动选择最优通道（WECHAT/ALIPAY/UNIONPAY）

### 3.2 查询订单
```bash
curl http://localhost:8080/api/payment/query/ORD20260718001
```

### 3.3 退款
```bash
curl -X POST http://localhost:8080/api/payment/refund \
  -H "Content-Type: application/json" \
  -d '{"orderNo": "ORD20260718001"}'
```
注意：仅 `SUCCESS` 状态的订单可退款。

### 3.4 手动触发对账
```bash
curl -X POST http://localhost:8080/api/reconciliation/trigger
```

### 3.5 查看对账异常
```bash
curl http://localhost:8080/api/reconciliation/errors?limit=20
```

---

## 4. 状态码速查

| 订单状态 | 说明 |
|---------|------|
| PENDING | 待支付 |
| SUCCESS | 支付成功 |
| FAILED | 支付失败（已尝试主通道+备用通道） |
| CLOSED | 已关闭 |
| REFUNDED | 已退款 |

| 响应 code | 说明 |
|-----------|------|
| 200 | 成功 |
| 400 | 参数错误 |
| 404 | 订单不存在 |
| 409 | 状态冲突（如对未支付订单退款） |
| 500 | 服务内部错误 |

---

## 5. 对账差异类型

| 类型 | 含义 | 处理建议 |
|------|------|---------|
| MATCH | 一致 | 无需处理 |
| LOCAL_ONLY | 本地有、渠道无 | 检查是否渠道回调丢失 |
| CHANNEL_ONLY | 渠道有、本地无 | 排查是否本地漏单 |
| AMOUNT_MISMATCH | 金额不一致 | 人工核实，可能涉及手续费计算差异 |

---

## 6. 故障排查

### 应用启动失败
```bash
# 检查基础设施是否就绪
docker ps

# 查看应用日志
./mvnw.cmd spring-boot:run 2>&1 | tail -50
```

### MySQL 连接失败
```bash
docker exec payment-mysql mysql -uroot -proot123 -e "SHOW DATABASES;"
```
确认 `payment_gateway` 库存在，否则手动执行：
```bash
docker exec -i payment-mysql mysql -uroot -proot123 < src/main/resources/db/schema.sql
```

### Redis 连接失败
```bash
docker exec payment-redis redis-cli PING
# 应返回 PONG
```

### RabbitMQ 管理界面
浏览器打开：`http://localhost:15672`（guest/guest）

---

## 7. 停止服务

```bash
# 停止应用：Ctrl+C

# 停止基础设施（保留数据）
docker-compose stop

# 停止并清空数据
docker-compose down -v
```

---

## 8. 定时任务

| 任务 | 时间 | 说明 |
|------|------|------|
| 每日对账 | 凌晨 2:00（Asia/Shanghai） | 自动拉取各渠道昨日账单并勾兑 |
| 异常告警 | 对账完成后自动 | 有差异时推送到 RabbitMQ `payment.alert` 队列 |
