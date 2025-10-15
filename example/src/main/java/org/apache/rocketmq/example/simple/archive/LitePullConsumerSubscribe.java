package org.apache.rocketmq.example.simple.archive;

import org.apache.rocketmq.client.consumer.DefaultLitePullConsumer;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.LitePullConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;

import java.util.List;

/**
 * @author zero
 * @description 测试 DefaultLitePullConsumer
 * @date 2025-10-15
 */
public class LitePullConsumerSubscribe {
    public static void main(String[] args) throws MQClientException {

        DefaultLitePullConsumer liteConsumer = new DefaultLitePullConsumer(
                "lite-consumer-test01");
        liteConsumer.setNamesrvAddr("localhost:9876");
        // 订阅 topic
        liteConsumer.subscribe("TestTopic", "*"); // * 代表接收所有。

        liteConsumer.start();

        try {
            while (true) {
                List<MessageExt> messages = liteConsumer.poll();
                System.out.println("===>" + messages);
            }
        } finally {
            liteConsumer.shutdown();
        }
    }
}
