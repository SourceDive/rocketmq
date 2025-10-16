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
import org.apache.rocketmq.store.config.MessageStoreConfig;
import org.apache.rocketmq.store.config.BrokerRole;
import org.apache.rocketmq.broker.BrokerController;
import org.apache.rocketmq.namesrv.NamesrvController;
import org.apache.rocketmq.common.namesrv.NamesrvConfig;
import org.apache.rocketmq.common.BrokerConfig;
import org.apache.rocketmq.remoting.netty.NettyServerConfig;
import org.apache.rocketmq.remoting.netty.NettyClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 嵌入式RocketMQ演示程序
 * 自动启动NameServer、Broker、生产者和消费者
 * 完全自动化，无需手动启动任何服务
 */
public class EmbeddedRocketMQDemo {
    
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedRocketMQDemo.class);
    private static final String NAMESRV_ADDR = "localhost:9876";
    private static final String TOPIC = "embedded-demo-topic";
    private static final String PRODUCER_GROUP = "embedded-demo-producer-group";
    private static final String CONSUMER_GROUP = "embedded-demo-consumer-group";
    
    private static final AtomicLong messageCount = new AtomicLong(0);
    private static volatile boolean running = true;
    
    private static NamesrvController namesrvController;
    private static BrokerController brokerController;
    
    public static void main(String[] args) {
        System.out.println("=== RocketMQ 嵌入式演示程序 ===");
        System.out.println("正在自动启动完整的RocketMQ环境...");
        System.out.println("按 Ctrl+C 停止演示");
        System.out.println("================================");
        
        // 添加关闭钩子
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n正在停止演示程序...");
            running = false;
            shutdown();
        }));
        
        try {
            // 启动NameServer
            startNameServer();
            
            // 启动Broker
            startBroker();
            
            // 等待服务启动
            Thread.sleep(3000);
            
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
        } finally {
            shutdown();
        }
        
        System.out.println("演示程序已结束，总共处理了 " + messageCount.get() + " 条消息");
    }
    
    /**
     * 启动NameServer
     */
    private static void startNameServer() throws Exception {
        System.out.println("启动NameServer...");
        
        NamesrvConfig namesrvConfig = new NamesrvConfig();
        namesrvConfig.setRocketmqHome(System.getProperty("user.dir"));
        
        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setListenPort(9876);
        
        namesrvController = new NamesrvController(namesrvConfig, nettyServerConfig);
        namesrvController.initialize();
        namesrvController.start();
        
        System.out.println("NameServer启动成功，端口: 9876");
    }
    
    /**
     * 启动Broker
     */
    private static void startBroker() throws Exception {
        System.out.println("启动Broker...");
        
        BrokerConfig brokerConfig = new BrokerConfig();
        brokerConfig.setBrokerName("embedded-broker");
        brokerConfig.setBrokerId(0);
        brokerConfig.setNamesrvAddr(NAMESRV_ADDR);
        brokerConfig.setRocketmqHome(System.getProperty("user.dir"));
        
        MessageStoreConfig messageStoreConfig = new MessageStoreConfig();
        messageStoreConfig.setStorePathRootDir(System.getProperty("user.dir") + File.separator + "target" + File.separator + "embedded-store");
        messageStoreConfig.setStorePathCommitLog(System.getProperty("user.dir") + File.separator + "target" + File.separator + "embedded-store" + File.separator + "commitlog");
        
        NettyServerConfig nettyServerConfig = new NettyServerConfig();
        nettyServerConfig.setListenPort(10911);
        
        NettyClientConfig nettyClientConfig = new NettyClientConfig();
        
        brokerController = new BrokerController(
                brokerConfig,
                nettyServerConfig,
                nettyClientConfig,
                messageStoreConfig
        );
        
        brokerController.initialize();
        brokerController.start();
        
        System.out.println("Broker启动成功，端口: 10911");
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
                String messageBody = "嵌入式演示消息 #" + i + " - 时间: " + java.time.LocalTime.now();
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
        
        // 保持生产者运行
        producer.shutdown();
    }
    
    /**
     * 关闭所有服务
     */
    private static void shutdown() {
        try {
            if (brokerController != null) {
                brokerController.shutdown();
                System.out.println("Broker已关闭");
            }
            
            if (namesrvController != null) {
                namesrvController.shutdown();
                System.out.println("NameServer已关闭");
            }
        } catch (Exception e) {
            logger.error("关闭服务时发生异常", e);
        }
    }
}

