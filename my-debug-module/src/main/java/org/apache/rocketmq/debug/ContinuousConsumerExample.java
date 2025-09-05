package org.apache.rocketmq.debug;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 持续运行的消费者示例
 * 支持优雅关闭和消息统计
 */
public class ContinuousConsumerExample {
    
    private static final Logger logger = LoggerFactory.getLogger(ContinuousConsumerExample.class);
    private static final AtomicLong messageCount = new AtomicLong(0);
    private static volatile boolean running = true;
    
    public static void main(String[] args) {
        // 创建消费者实例
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("my-debug-consumer-group");
        
        // 设置NameServer地址
        consumer.setNamesrvAddr("localhost:9876");
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在关闭消费者...");
            running = false;
            consumer.shutdown();
            System.out.println("消费者已关闭，总共处理了 " + messageCount.get() + " 条消息");
        }));
        
        try {
            // 订阅主题
            consumer.subscribe("my-debug-topic", "*");
            
            // 注册消息监听器
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                               ConsumeConcurrentlyContext context) {
                    for (MessageExt message : messages) {
                        long count = messageCount.incrementAndGet();
                        
                        System.out.println("=== 收到消息 #" + count + " ===");
                        System.out.println("Topic: " + message.getTopic());
                        System.out.println("Tag: " + message.getTags());
                        System.out.println("消息ID: " + message.getMsgId());
                        System.out.println("消息内容: " + new String(message.getBody()));
                        System.out.println("队列ID: " + message.getQueueId());
                        System.out.println("存储时间: " + new java.util.Date(message.getStoreTimestamp()));
                        System.out.println("========================");
                        
                        logger.info("处理消息: topic={}, tag={}, body={}, msgId={}", 
                                message.getTopic(), 
                                message.getTags(), 
                                new String(message.getBody()),
                                message.getMsgId());
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            
            // 启动消费者
            consumer.start();
            logger.info("消费者启动成功，开始持续监听消息...");
            
            System.out.println("=== RocketMQ 持续消费者示例 ===");
            System.out.println("消费者已启动，正在监听 'my-debug-topic' 主题");
            System.out.println("按 Ctrl+C 或关闭终端来停止消费者");
            System.out.println("================================");
            
            // 保持程序运行
            while (running) {
                Thread.sleep(1000);
                
                // 每10秒显示一次统计信息
                if (messageCount.get() > 0 && messageCount.get() % 10 == 0) {
                    System.out.println("已处理消息数量: " + messageCount.get());
                }
            }
            
        } catch (MQClientException e) {
            logger.error("MQ客户端异常", e);
        } catch (InterruptedException e) {
            logger.info("消费者被中断");
        } catch (Exception e) {
            logger.error("消费者异常", e);
        } finally {
            if (running) {
                consumer.shutdown();
                logger.info("消费者已关闭");
            }
        }
    }
}
