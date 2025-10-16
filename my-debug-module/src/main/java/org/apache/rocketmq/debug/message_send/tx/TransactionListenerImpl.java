package org.apache.rocketmq.debug.message_send.tx;

import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author zero
 * @description 源码仓库中有现成的实现，这里就不重复了。
 * @date 2025-09-18
 * @see org.apache.rocketmq.example.transaction.TransactionProducer
 */
public class TransactionListenerImpl implements TransactionListener {
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        return null;
    }

    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        return null;
    }
}
