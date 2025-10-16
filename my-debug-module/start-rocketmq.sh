#!/bin/bash

# RocketMQ 自动启动脚本

echo "=== RocketMQ 自动启动脚本 ==="

# 检查是否在正确的目录
if [ ! -f "../distribution/bin/mqnamesrv" ]; then
    echo "错误: 请在 my-debug-module 目录下运行此脚本"
    exit 1
fi

# 设置环境变量
export ROCKETMQ_HOME=$(pwd)/..
export JAVA_OPT="${JAVA_OPT} -server -Xms256m -Xmx256m -Xmn128m"

# 创建日志目录
mkdir -p logs

echo "启动NameServer..."
nohup sh ../distribution/bin/mqnamesrv > logs/namesrv.log 2>&1 &
NAMESRV_PID=$!
echo "NameServer已启动，PID: $NAMESRV_PID"

# 等待NameServer启动
sleep 3

echo "启动Broker..."
nohup sh ../distribution/bin/mqbroker -n localhost:9876 -c ../distribution/conf/broker.conf > logs/broker.log 2>&1 &
BROKER_PID=$!
echo "Broker已启动，PID: $BROKER_PID"

# 等待Broker启动
sleep 5

echo "RocketMQ服务启动完成！"
echo "NameServer PID: $NAMESRV_PID"
echo "Broker PID: $BROKER_PID"
echo "日志文件: logs/namesrv.log, logs/broker.log"
echo ""
echo "现在可以运行演示程序了："
echo "mvn exec:java -Dexec.mainClass=\"org.apache.rocketmq.debug.SimpleAutoDemo\""
echo ""
echo "按 Ctrl+C 停止所有服务"

# 等待用户中断
trap 'echo "正在停止服务..."; kill $NAMESRV_PID $BROKER_PID; echo "服务已停止"; exit 0' INT

# 保持脚本运行
while true; do
    sleep 1
done

