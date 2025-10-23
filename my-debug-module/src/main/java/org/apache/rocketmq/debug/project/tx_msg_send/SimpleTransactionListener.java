package org.apache.rocketmq.debug.project.tx_msg_send;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * 简单的事务监听器实现
 * 这是事务消息的核心组件，用于处理本地事务的执行和回查
 */
public class SimpleTransactionListener implements TransactionListener {
    
    /**
     * 执行本地事务
     * @param msg 消息
     * @param arg 参数
     * @return 本地事务状态
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        System.out.println("开始执行本地事务，消息ID: " + msg.getTransactionId());
        System.out.println("消息内容: " + new String(msg.getBody()));
        
        try {
            // 模拟本地业务逻辑
            // 这里可以执行数据库操作、调用其他服务等
            System.out.println("执行本地业务逻辑...");
            
            // 模拟业务处理时间
            Thread.sleep(1000);
            
            // 模拟业务处理结果
            // 这里可以根据实际业务逻辑返回不同的状态
            boolean businessSuccess = true; // 模拟业务成功
            
            if (businessSuccess) {
                System.out.println("本地事务执行成功，提交消息");
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                System.out.println("本地事务执行失败，回滚消息");
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
            
        } catch (Exception e) {
            System.out.println("本地事务执行异常: " + e.getMessage());
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }
    
    /**
     * 回查本地事务状态
     * 当消息状态为UNKNOWN时，RocketMQ会定期回查
     * @param msg 消息
     * @return 本地事务状态
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        System.out.println("回查本地事务状态，消息ID: " + msg.getTransactionId());
        
        // 这里应该查询本地事务的实际状态
        // 可以通过消息ID查询数据库或其他存储来确定事务状态
        try {
            // 模拟查询本地事务状态
            // 实际应用中，这里应该查询数据库或缓存
            System.out.println("查询本地事务状态...");
            
            // 模拟查询结果
            boolean transactionCommitted = true; // 模拟事务已提交
            
            if (transactionCommitted) {
                System.out.println("本地事务已提交，提交消息");
                return LocalTransactionState.COMMIT_MESSAGE;
            } else {
                System.out.println("本地事务未提交，回滚消息");
                return LocalTransactionState.ROLLBACK_MESSAGE;
            }
            
        } catch (Exception e) {
            System.out.println("回查本地事务状态异常: " + e.getMessage());
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
    }
}
