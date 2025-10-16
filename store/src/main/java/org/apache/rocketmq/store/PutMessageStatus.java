/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.rocketmq.store;

/**
 * 消息写入状态。
 */
public enum PutMessageStatus {
    /// 成功
    PUT_OK,    // 写入成功
    FLUSH_DISK_TIMEOUT, // 刷盘超时(但消息已写入)
    FLUSH_SLAVE_TIMEOUT, // 从节点同步超时
    SLAVE_NOT_AVAILABLE, // 从节点不可用

    /// 失败
    SERVICE_NOT_AVAILABLE, // 服务不可用
    CREATE_MAPEDFILE_FAILED, // 创建映射文件失败
    MESSAGE_ILLEGAL, // 消息非法
    PROPERTIES_SIZE_EXCEEDED, // 属性大小超限
    OS_PAGECACHE_BUSY, // 操作系统页缓存繁忙
    UNKNOWN_ERROR, // 未知错误
    LMQ_CONSUME_QUEUE_NUM_EXCEEDED,
}
