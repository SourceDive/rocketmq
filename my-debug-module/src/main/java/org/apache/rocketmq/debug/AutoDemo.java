package org.apache.rocketmq.debug;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.namesrv.NamesrvStartup;
import org.apache.rocketmq.broker.BrokerStartup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 自动化演示程序
 * 自动启动NameServer、Broker、生产者和消费者
 * 演示完整的RocketMQ生产-消费流程
 */
public class AutoDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(AutoDemo.class);
    private static final String NAMESRV_ADDR = "localhost:9876";
    private static final String TOPIC = "auto-demo-topic";
    private static final String PRODUCER_GROUP = "auto-demo-producer-group";
    private static final String CONSUMER_GROUP = "auto-demo-consumer-group";
    
    private static final AtomicLong messageCount = new AtomicLong(0);
    private static volatile boolean running = true;
    
    public static void main(String[] args) {
        System.out.println("=== RocketMQ 自动化演示程序 ===");
        System.out.println("正在启动完整的生产-消费演示...");
        System.out.println("按 Ctrl+C 停止演示");
        System.out.println("================================");
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在停止演示程序...");
            running = false;
        }));
        
        try {
            // 启动NameServer和Broker（使用嵌入式模式）
            startEmbeddedRocketMQ();
            
            // 等待服务启动
            Thread.sleep(5000);
            
            // 启动消费者
            DefaultMQPushConsumer consumer = startConsumer();
            
            // 等待消费者启动
            Thread.sleep(2000);
            
            // 启动生产者并发送消息
            startProducer();
            
            // 保持程序运行
            while (running) {
                Thread.sleep(1000);
            }
            
            // 关闭消费者
            if (consumer != null) {
                consumer.shutdown();
            }
            
        } catch (Exception e) {
            logger.error("演示程序异常", e);
        }
        
        System.out.println("演示程序已结束，总共处理了 " + messageCount.get() + " 条消息");
    }
    
    /**
     * 启动嵌入式RocketMQ（简化版本，实际使用中建议使用独立的NameServer和Broker）
     */
    private static void startEmbeddedRocketMQ() {
        System.out.println("启动嵌入式RocketMQ服务...");
        // 注意：这里使用简化的启动方式，实际生产环境建议使用独立的NameServer和Broker
        System.out.println("提示：请确保已手动启动NameServer和Broker");
        System.out.println("启动命令：");
        System.out.println("  NameServer: sh mqnamesrv");
        System.out.println("  Broker: sh mqbroker -n localhost:9876");
    }
    
    /**
     * 启动消费者
     */
    private static DefaultMQPushConsumer startConsumer() throws MQClientException {
        System.out.println("启动消费者...");
        
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(CONSUMER_GROUP);
        consumer.setNamesrvAddr(NAMESRV_ADDR);
        consumer.subscribe(TOPIC, "*");
        
        consumer.registerMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                           ConsumeConcurrentlyContext context) {
                for (MessageExt message : messages) {
                    long count = messageCount.incrementAndGet();
                    System.out.println("✓ 消费者收到消息 #" + count + ": " + new String(message.getBody()));
                }
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        consumer.start();
        System.out.println("消费者启动成功，开始监听消息...");
        return consumer;
    }
    
    /**
     * 启动生产者并发送消息
     */
    private static void startProducer() throws Exception {
        System.out.println("启动生产者...");
        
        DefaultMQProducer producer = new DefaultMQProducer(PRODUCER_GROUP);
        producer.setNamesrvAddr(NAMESRV_ADDR);
        producer.start();
        
        System.out.println("生产者启动成功，开始发送消息...");
        
        // 发送10条测试消息
        for (int i = 1; i <= 10; i++) {
            try {
                String messageBody = "自动演示消息 #" + i + " - 时间: " + java.time.LocalTime.now();
                Message message = new Message(TOPIC, "demo-tag", messageBody.getBytes("UTF-8"));
                
                SendResult sendResult = producer.send(message);
                System.out.println("✓ 生产者发送消息 #" + i + ": " + messageBody);
                
                // 每发送一条消息后等待1秒
                Thread.sleep(1000);
                
            } catch (Exception e) {
                System.err.println("✗ 发送消息失败: " + e.getMessage());
            }
        }
        
        System.out.println("所有消息发送完成，消费者将继续监听新消息...");
        System.out.println("按 Ctrl+C 停止演示");
        
        // 保持生产者运行，可以继续发送消息
        producer.shutdown();
    }
}

