# RocketMQ 事务消息发送示例

这是一个最简单的RocketMQ事务消息发送示例，包含以下文件：

## 文件说明

1. **SimpleTransactionListener.java** - 事务监听器
   - 实现 `TransactionListener` 接口
   - `executeLocalTransaction()` - 执行本地事务
   - `checkLocalTransaction()` - 回查本地事务状态

2. **SimpleTransactionProducer.java** - 事务消息生产者
   - 创建 `TransactionMQProducer`
   - 设置事务监听器
   - 发送事务消息

3. **SimpleTransactionConsumer.java** - 事务消息消费者
   - 创建 `DefaultMQPushConsumer`
   - 接收和消费事务消息

## 运行步骤

### 1. 启动RocketMQ服务
```bash
# 启动NameServer
sh mqnamesrv

# 启动Broker
sh mqbroker -n 127.0.0.1:9876
```

### 2. 运行消费者
```bash
# 先运行消费者，等待消息
java -cp target/classes org.apache.rocketmq.debug.project.tx_msg_send.SimpleTransactionConsumer
```

### 3. 运行生产者
```bash
# 运行生产者，发送事务消息
java -cp target/classes org.apache.rocketmq.debug.project.tx_msg_send.SimpleTransactionProducer
```

## 事务消息流程

1. **发送阶段**：生产者发送半消息到RocketMQ
2. **本地事务**：执行本地业务逻辑
3. **提交/回滚**：根据本地事务结果决定消息状态
4. **回查机制**：如果状态为UNKNOWN，RocketMQ会定期回查
5. **消费阶段**：消费者接收并处理消息

## 关键概念

- **半消息**：事务消息的中间状态，对消费者不可见
- **本地事务**：与消息发送相关的业务逻辑
- **事务状态**：
  - `COMMIT_MESSAGE` - 提交消息，消费者可见
  - `ROLLBACK_MESSAGE` - 回滚消息，丢弃消息
  - `UNKNOWN` - 未知状态，需要回查

## 注意事项

1. 确保RocketMQ服务正常运行
2. 先启动消费者，再启动生产者
3. 本地事务逻辑要保证幂等性
4. 回查逻辑要能准确判断事务状态
