package org.apache.rocketmq.debug.archive;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 简单的生产者示例
 * 用于学习RocketMQ的生产者API
 */
public class SimpleProducerExample {
    
    private static final Logger logger = LoggerFactory.getLogger(SimpleProducerExample.class);
    
    public static void main(String[] args) {
        // 创建生产者实例
        DefaultMQProducer producer = new DefaultMQProducer("my-debug-producer-group");
        
        // 设置NameServer地址
        producer.setNamesrvAddr("localhost:9876");
        
        try {
            // 启动生产者
            producer.start();
            logger.info("生产者启动成功");
            
            // 创建消息
            String topic = "TestTopic";
            String tag = "my-debug-tag";
            String body = "Hello RocketMQ from my-debug-module!";
            
            Message message = new Message(topic, tag, body.getBytes());
            
            // 发送消息
            SendResult sendResult;
            while (true) {
                Thread.sleep(3000);
                sendResult = producer.send(message);
                logger.info("消息发送成功: {}", sendResult);
            }


        } catch (MQClientException e) {
            logger.error("MQ客户端异常", e);
        } catch (Exception e) {
            logger.error("发送消息异常", e);
        } finally {
            // 关闭生产者
            producer.shutdown();
            logger.info("生产者已关闭");
        }
    }

    public SendResult snedOneMessage(Message message, SendResult sendResult, DefaultMQProducer producer) throws MQBrokerException, RemotingException, InterruptedException, MQClientException {
        sendResult = producer.send(message);
        logger.info("消息发送成功: {}", sendResult);

        return sendResult;
    }

    private SendResult alwaysSend(Message message, SendResult sendResult, DefaultMQProducer producer) throws InterruptedException, MQBrokerException, RemotingException, MQClientException {
        while (true) {
            Thread.sleep(3000);
            sendResult = producer.send(message);
            logger.info("消息发送成功: {}", sendResult);
        }
    }
}
