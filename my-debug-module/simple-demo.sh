#!/bin/bash

# RocketMQ 简单演示脚本
# 使用Java直接启动NameServer和Broker

echo "=== RocketMQ 简单演示脚本 ==="
echo "正在启动完整的RocketMQ演示环境..."
echo ""

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    echo "错误: 请在 my-debug-module 目录下运行此脚本"
    exit 1
fi

# 创建日志目录
mkdir -p logs

# 设置环境变量
export ROCKETMQ_HOME=$(pwd)/..
export JAVA_OPT="${JAVA_OPT} -server -Xms256m -Xmx256m -Xmn128m"

echo "1. 启动NameServer..."
cd ..
java -cp "namesrv/target/classes:common/target/classes:remoting/target/classes:logging/target/classes:client/target/classes" \
     org.apache.rocketmq.namesrv.NamesrvStartup > my-debug-module/logs/namesrv.log 2>&1 &
NAMESRV_PID=$!
echo "NameServer已启动，PID: $NAMESRV_PID"

# 等待NameServer启动
sleep 3

echo "2. 启动Broker..."
java -cp "broker/target/classes:store/target/classes:namesrv/target/classes:common/target/classes:remoting/target/classes:logging/target/classes:client/target/classes" \
     org.apache.rocketmq.broker.BrokerStartup \
     -n localhost:9876 \
     -c distribution/conf/broker.conf > my-debug-module/logs/broker.log 2>&1 &
BROKER_PID=$!
echo "Broker已启动，PID: $BROKER_PID"

# 等待Broker启动
sleep 5

echo "RocketMQ服务启动完成！"
echo ""

# 回到my-debug-module目录
cd my-debug-module

echo "3. 启动演示程序..."
echo "正在启动生产-消费演示..."
echo ""

# 添加关闭钩子
trap 'echo "正在停止服务..."; kill $NAMESRV_PID $BROKER_PID 2>/dev/null; echo "服务已停止"; exit 0' INT

# 运行演示程序
mvn exec:java -Dexec.mainClass="org.apache.rocketmq.debug.SimpleAutoDemo" -q

