package org.apache.rocketmq.debug.project.tx_msg_send;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.TransactionMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * 简单的事务消息发送示例
 * 演示如何使用RocketMQ发送事务消息
 */
public class SimpleTransactionProducer {
    
    public static void main(String[] args) throws MQClientException, InterruptedException {
        // 强制使用IPv4，禁用IPv6
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.net.preferIPv6Addresses", "false");
        
        // 创建事务消息生产者
        TransactionMQProducer producer = new TransactionMQProducer("tx_producer_group");
        
        // 设置NameServer地址 - 使用localhost避免IPv6问题
        producer.setNamesrvAddr("127.0.0.1:9876");
        
        // 设置事务监听器
        producer.setTransactionListener(new SimpleTransactionListener());
        
        // 设置线程池，用于执行本地事务
        producer.setExecutorService(java.util.concurrent.Executors.newFixedThreadPool(2));
        
        // 注意：TransactionMQProducer没有setSendMsgTimeout方法
        
        // 启动生产者
        try {
            producer.start();
            System.out.println("事务消息生产者启动成功");
        } catch (Exception e) {
            System.err.println("启动生产者失败: " + e.getMessage());
            e.printStackTrace();
            return;
        }
        
        try {
            // 发送事务消息
            for (int i = 0; i < 3; i++) {
                // 创建消息
                String messageBody = "事务消息内容 - " + i;
                Message msg = new Message(
                    "tx_topic",                    // 主题
                    "tagA",                        // 标签
                    messageBody.getBytes(RemotingHelper.DEFAULT_CHARSET)
                );
                
                // 设置消息属性（可选）
                msg.putUserProperty("orderId", "ORDER_" + i);
                msg.putUserProperty("amount", String.valueOf(100 + i * 10));
                
                // 发送事务消息
                // 第二个参数是本地事务的参数，会传递给executeLocalTransaction方法
                SendResult sendResult = producer.sendMessageInTransaction(msg, "事务参数_" + i);
                
                System.out.println("发送事务消息结果: " + sendResult);
                System.out.println("消息ID: " + sendResult.getMsgId());
                System.out.println("事务ID: " + sendResult.getTransactionId());
                System.out.println("---");
                
                // 等待一段时间，观察事务处理过程
                Thread.sleep(2000);
            }
            
        } catch (Exception e) {
            System.err.println("发送事务消息失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭生产者
            producer.shutdown();
            System.out.println("事务消息生产者已关闭");
        }
    }
}
