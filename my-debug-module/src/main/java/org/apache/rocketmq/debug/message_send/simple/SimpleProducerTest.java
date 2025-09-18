package org.apache.rocketmq.debug.message_send.simple;

import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.common.RemotingHelper;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zero
 * @description 消息发送实战
 * @date 2025-09-18
 */
@DisplayName("消息发送实战")
public class SimpleProducerTest {

    @Test
    @DisplayName("发送普通消息")
    public void testNormalSend() throws MQClientException, UnsupportedEncodingException, MQBrokerException, RemotingException, InterruptedException {
        DefaultMQProducer producer = new DefaultMQProducer(
                "MyProducerBatchGroup");
        producer.setNamesrvAddr("127.0.0.1:9876");
        producer.start();

        Message msg = new Message("MessageTest",
                ("hello batch").getBytes(RemotingHelper.DEFAULT_CHARSET));
        SendResult result = producer.send(msg);
        System.out.println("发送结果：" + result);

        producer.shutdown();
    }

    @Test
    @DisplayName("发送批量消息")
    public void testBatchSend() throws MQClientException,
                                UnsupportedEncodingException, MQBrokerException, RemotingException, InterruptedException {
        // 创建生产者实例
        DefaultMQProducer producer = new DefaultMQProducer("MyProducerGroup");
        // 设置地址
        producer.setNamesrvAddr("127.0.0.1:9876");
        // 启动生产者
        producer.start();

        String topic = "BatchTopic";
        List<Message> msgList = new ArrayList<Message>();
        for (int i = 0; i < 10; i++) {
            Message msg = new Message(topic, "TAG", "order" + i,
                    ("hello batch").getBytes(RemotingHelper.DEFAULT_CHARSET));
            msgList.add(msg);
        }

        SendResult result = producer.send(msgList);
        System.out.println("发送结果：" + result);

        // 关闭生产者
        producer.shutdown();
    }


}
