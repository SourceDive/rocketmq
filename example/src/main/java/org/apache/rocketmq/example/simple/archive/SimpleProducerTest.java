package org.apache.rocketmq.example.simple.archive;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;

/**
 * @author zero
 * @description 简单生产者测试
 * @date 2025-10-15
 */
public class SimpleProducerTest {
    public static void main(String[] args) throws Exception {
        // 创建生产者
        DefaultMQProducer producer = new DefaultMQProducer("producer-group-test");
        producer.setNamesrvAddr("localhost:9876");
        
        // 启动生产者
        producer.start();
        
        try {
            // 发送消息
            for (int i = 0; i < 5; i++) {
                Message msg = new Message("TestTopic", 
                    "TagA", 
                    ("Hello RocketMQ " + i).getBytes(RemotingHelper.DEFAULT_CHARSET));
                
                SendResult sendResult = producer.send(msg);
                System.out.printf("发送结果: %s%n", sendResult);
            }
        } finally {
            producer.shutdown();
        }
    }
}



