package org.apache.rocketmq.debug;

import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * 持续运行的生产者示例
 * 支持交互式发送消息和自动发送消息两种模式
 */
public class ContinuousProducerExample {
    
    private static final Logger logger = LoggerFactory.getLogger(ContinuousProducerExample.class);
    
    public static void main(String[] args) {
        // 创建生产者实例
        DefaultMQProducer producer = new DefaultMQProducer("my-debug-producer-group");
        
        // 设置NameServer地址
        producer.setNamesrvAddr("localhost:9876");
        
        try {
            // 启动生产者
            producer.start();
            logger.info("生产者启动成功，开始持续运行...");
            
            Scanner scanner = new Scanner(System.in);
            int messageCount = 0;
            
            System.out.println("=== RocketMQ 持续生产者示例 ===");
            System.out.println("1. 输入消息内容发送消息");
            System.out.println("2. 输入 'auto' 开启自动发送模式");
            System.out.println("3. 输入 'quit' 或 'exit' 退出程序");
            System.out.println("================================");
            
            while (true) {
                System.out.print("请选择模式 (输入消息/auto/quit): ");
                String input = scanner.nextLine().trim();
                
                if ("quit".equalsIgnoreCase(input) || "exit".equalsIgnoreCase(input)) {
                    logger.info("用户选择退出");
                    break;
                }
                
                if ("auto".equalsIgnoreCase(input)) {
                    // 自动发送模式
                    autoSendMode(producer, scanner);
                    continue;
                }
                
                if (input.isEmpty()) {
                    System.out.println("输入不能为空，请重新输入");
                    continue;
                }
                
                try {
                    // 手动发送消息
                    sendMessage(producer, input, ++messageCount);
                    System.out.println("✓ 消息发送成功");
                    
                } catch (Exception e) {
                    logger.error("发送消息异常", e);
                    System.out.println("✗ 消息发送失败: " + e.getMessage());
                }
            }
            
            scanner.close();
            
        } catch (MQClientException e) {
            logger.error("MQ客户端异常", e);
        } catch (Exception e) {
            logger.error("生产者启动异常", e);
        } finally {
            // 关闭生产者
            producer.shutdown();
            logger.info("生产者已关闭");
        }
    }
    
    /**
     * 自动发送模式
     */
    private static void autoSendMode(DefaultMQProducer producer, Scanner scanner) {
        System.out.println("进入自动发送模式，每3秒发送一条消息");
        System.out.println("输入任意内容停止自动发送");
        
        Thread autoSendThread = new Thread(() -> {
            int count = 0;
            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Thread.sleep(3000);
                    String message = String.format("自动消息 #%d - 时间: %s", 
                            ++count, java.time.LocalTime.now());
                    sendMessage(producer, message, count);
                    System.out.println("✓ 自动发送消息 #" + count);
                }
            } catch (InterruptedException e) {
                System.out.println("自动发送已停止");
            } catch (Exception e) {
                logger.error("自动发送消息异常", e);
            }
        });
        
        autoSendThread.start();
        
        // 等待用户输入停止自动发送
        scanner.nextLine();
        autoSendThread.interrupt();
        System.out.println("已退出自动发送模式");
    }
    
    /**
     * 发送消息
     */
    private static void sendMessage(DefaultMQProducer producer, String content, int messageId) throws Exception {
        String topic = "my-debug-topic";
        String tag = "my-debug-tag";
        String body = String.format("消息#%d: %s", messageId, content);
        
        Message message = new Message(topic, tag, body.getBytes("UTF-8"));
        SendResult sendResult = producer.send(message);
        logger.info("消息发送成功: {}", sendResult);
    }
}
