#!/bin/bash

# RocketMQ 完整演示程序
# 自动设置环境变量、启动服务、运行演示

echo "=== RocketMQ 完整演示程序 ==="
echo "正在启动完整的RocketMQ演示环境..."
echo ""

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    echo "错误: 请在 my-debug-module 目录下运行此脚本"
    exit 1
fi

# 设置环境变量
export ROCKETMQ_HOME=$(pwd)/..
export JAVA_OPT="${JAVA_OPT} -server -Xms256m -Xmx256m -Xmn128m"

echo "环境变量设置完成:"
echo "  ROCKETMQ_HOME = $ROCKETMQ_HOME"
echo ""

# 创建日志目录
mkdir -p logs

# 停止可能存在的旧进程
echo "清理旧进程..."
pkill -f NamesrvStartup 2>/dev/null || true
pkill -f BrokerStartup 2>/dev/null || true
sleep 2

echo "1. 启动NameServer..."
cd ..
java -cp "namesrv/target/classes:common/target/classes:remoting/target/classes:logging/target/classes:client/target/classes" \
     org.apache.rocketmq.namesrv.NamesrvStartup > my-debug-module/logs/namesrv.log 2>&1 &
NAMESRV_PID=$!
echo "NameServer已启动，PID: $NAMESRV_PID"

# 等待NameServer启动
echo "等待NameServer启动..."
sleep 5

# 检查NameServer是否启动成功
if ! ps -p $NAMESRV_PID > /dev/null; then
    echo "NameServer启动失败，请检查日志:"
    cat my-debug-module/logs/namesrv.log
    exit 1
fi

echo "2. 启动Broker..."
java -cp "broker/target/classes:store/target/classes:namesrv/target/classes:common/target/classes:remoting/target/classes:logging/target/classes:client/target/classes" \
     org.apache.rocketmq.broker.BrokerStartup \
     -n localhost:9876 \
     -c distribution/conf/broker.conf > my-debug-module/logs/broker.log 2>&1 &
BROKER_PID=$!
echo "Broker已启动，PID: $BROKER_PID"

# 等待Broker启动
echo "等待Broker启动..."
sleep 5

# 检查Broker是否启动成功
if ! ps -p $BROKER_PID > /dev/null; then
    echo "Broker启动失败，请检查日志:"
    cat my-debug-module/logs/broker.log
    exit 1
fi

echo "RocketMQ服务启动完成！"
echo "NameServer PID: $NAMESRV_PID"
echo "Broker PID: $BROKER_PID"
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
