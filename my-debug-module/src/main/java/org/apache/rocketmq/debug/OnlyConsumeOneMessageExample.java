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
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 简单的消费者示例
 * 只让一个线程消费、且只消费一条消息，方便我debug测试。
 */
public class OnlyConsumeOneMessageExample {
    
    private static final Logger logger = LoggerFactory.getLogger(OnlyConsumeOneMessageExample.class);
    
    public static void main(String[] args) {
        // 创建消费者实例
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("my-debug-consumer-group");
        
        // 设置NameServer地址
        consumer.setNamesrvAddr("localhost:9876");
        
        // 用于控制是否继续消费的标记
        AtomicBoolean shouldContinue = new AtomicBoolean(true);
        // 确保只有一个线程能处理消息
        AtomicBoolean messageProcessed = new AtomicBoolean(false);
        
        try {
            // 订阅主题
            consumer.subscribe("TestTopic", "*");
            
            // 注册消息监听器
            consumer.registerMessageListener(new MessageListenerConcurrently() {
                @Override
                public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                               ConsumeConcurrentlyContext context) {
                    // 使用 CAS 操作确保只有一个线程能处理消息
                    if (!messages.isEmpty() && messageProcessed.compareAndSet(false, true)) {
                        MessageExt message = messages.get(0);
                        logger.info("收到消息: topic={}, tag={}, body={}, msgId={}",
                                message.getTopic(), 
                                message.getTags(), 
                                new String(message.getBody()),
                                message.getMsgId());
                        
                        // 消费一条消息后，设置标记为false，准备退出
                        shouldContinue.set(false);
                        logger.info("已消费一条消息，准备退出...");
                    } else {
                        logger.info("消息已被其他线程处理，跳过此消息");
                    }
                    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
                }
            });
            
            // 启动消费者
            consumer.start();
            logger.info("消费者启动成功，等待消息...");
            
            // 等待消费一条消息后退出
            // 异步线程没有修改，则一直进行等待。
            while (shouldContinue.get()) {
                Thread.sleep(100); // 短暂休眠，避免CPU占用过高
            }
            
            logger.info("已消费一条消息，程序即将退出");
            
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
