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

/**
 * 简单的消费者示例
 * 用于学习RocketMQ的消费者API
 */
public class SimpleConsumerExample {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleConsumerExample.class);
    
    public static void main(String[] args) {
        // 创建消费者实例
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("my-debug-consumer-group");
        
        // 设置NameServer地址
        consumer.setNamesrvAddr("localhost:9876");
        
        try {
            // 订阅主题
            consumer.subscribe("my-debug-topic", "*");
            
            // 注册消息监听器
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                               ConsumeConcurrentlyContext context) {
                    for (MessageExt message : messages) {
                        logger.info("收到消息: topic={}, tag={}, body={}", 
                                message.getTopic(), 
                                message.getTags(), 
                                new String(message.getBody()));
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            
            // 启动消费者
            consumer.start();
            logger.info("消费者启动成功，等待消息...");
            
            // 保持程序运行
            Thread.sleep(60000); // 运行60秒
            
        } catch (MQClientException e) {
            logger.error("MQ客户端异常", e);
        } catch (InterruptedException e) {
            logger.error("线程中断异常", e);
        } finally {
            // 关闭消费者
            consumer.shutdown();
            logger.info("消费者已关闭");
        }
    }
}
