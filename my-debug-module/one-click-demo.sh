#!/bin/bash

# RocketMQ 一键演示脚本

echo "=== RocketMQ 一键演示脚本 ==="
echo "正在启动完整的RocketMQ演示环境..."
echo ""

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    echo "错误: 请在 my-debug-module 目录下运行此脚本"
    exit 1
fi

# 编译项目
echo "1. 编译项目..."
echo "编译RocketMQ源码..."
cd .. && mvn clean compile -q -pl namesrv,broker,common,remoting,store,client
if [ $? -ne 0 ]; then
    echo "RocketMQ源码编译失败，请检查错误信息"
    exit 1
fi

echo "编译调试模块..."
mvn clean compile -q -pl my-debug-module
if [ $? -ne 0 ]; then
    echo "调试模块编译失败，请检查错误信息"
    exit 1
fi
echo "编译成功！"
echo ""

# 回到my-debug-module目录
cd my-debug-module

# 启动RocketMQ服务
echo "2. 启动RocketMQ服务..."

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
echo ""

# 启动演示程序
echo "3. 启动演示程序..."
echo "正在启动生产-消费演示..."
echo ""

# 添加关闭钩子
trap 'echo "正在停止服务..."; kill $NAMESRV_PID $BROKER_PID 2>/dev/null; echo "服务已停止"; exit 0' INT

# 运行演示程序
mvn exec:java -Dexec.mainClass="org.apache.rocketmq.debug.SimpleAutoDemo" -q
