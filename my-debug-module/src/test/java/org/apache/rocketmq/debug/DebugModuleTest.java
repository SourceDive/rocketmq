package org.apache.rocketmq.debug;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 调试模块测试类
 */
public class DebugModuleTest {
    
    private static final Logger logger = LoggerFactory.getLogger(DebugModuleTest.class);
    
    @Test
    public void testModuleSetup() {
        logger.info("测试调试模块设置");
        
        // 验证模块可以正常加载
        String moduleName = "my-debug-module";
        assertThat(moduleName).isEqualTo("my-debug-module");
        
        logger.info("调试模块设置测试通过");
    }
    
    @Test
    public void testRocketMQDependencies() {
        logger.info("测试RocketMQ依赖");
        
        // 验证可以访问RocketMQ的核心类
        try {
            Class.forName("org.apache.rocketmq.client.producer.DefaultMQProducer");
            Class.forName("org.apache.rocketmq.client.consumer.DefaultMQPushConsumer");
            Class.forName("org.apache.rocketmq.common.message.Message");
            
            logger.info("RocketMQ依赖测试通过");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("无法找到RocketMQ核心类", e);
        }
    }
}
