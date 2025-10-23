package org.apache.rocketmq.debug.project.tx_msg_send;

import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * 简单的事务消息消费者
 * 用于接收和消费事务消息
 */
public class SimpleTransactionConsumer {
    
    public static void main(String[] args) throws MQClientException, InterruptedException {
        // 强制使用IPv4，禁用IPv6
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
        
        // 创建消费者
        DefaultMQPushConsumer consumer = new DefaultMQPushConsumer("tx_consumer_group");
        
        // 设置NameServer地址 - 使用localhost避免IPv6问题
        consumer.setNamesrvAddr("localhost:9876");
        
        // 订阅主题和标签
        consumer.subscribe("tx_topic", "*"); // 订阅所有标签
        
        // 设置消息监听器
        consumer.setMessageListener(new MessageListenerConcurrently() {
            @Override
            public ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages,
                                                          ConsumeConcurrentlyContext context) {
                
                for (MessageExt message : messages) {
                    System.out.println("=== 收到事务消息 ===");
                    System.out.println("消息ID: " + message.getMsgId());
                    System.out.println("事务ID: " + message.getTransactionId());
                    System.out.println("主题: " + message.getTopic());
                    System.out.println("标签: " + message.getTags());
                    System.out.println("消息内容: " + new String(message.getBody()));
                    System.out.println("消息属性: " + message.getProperties());
                    System.out.println("消息队列: " + message.getQueueId());
                    System.out.println("消息偏移量: " + message.getQueueOffset());
                    System.out.println("==================");
                }
                
                // 返回消费状态
                return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
            }
        });
        
        // 启动消费者
        consumer.start();
        
        System.out.println("事务消息消费者启动成功，等待消息...");
        
        // 保持运行
        Thread.sleep(Long.MAX_VALUE);
    }
}
