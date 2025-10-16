#!/bin/bash

# RocketMQ 快速演示脚本
# 直接运行演示程序，假设NameServer和Broker已启动

echo "=== RocketMQ 快速演示脚本 ==="
echo "正在启动生产-消费演示..."
echo ""

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    echo "错误: 请在 my-debug-module 目录下运行此脚本"
    exit 1
fi

echo "注意：请确保NameServer和Broker已启动"
echo "如果没有启动，请先运行以下命令："
echo "  cd .."
echo "  java -cp \"namesrv/target/classes:common/target/classes:remoting/target/classes:logging/target/classes:client/target/classes\" org.apache.rocketmq.namesrv.NamesrvStartup &"
echo "  java -cp \"broker/target/classes:store/target/classes:namesrv/target/classes:common/target/classes:remoting/target/classes:logging/target/classes:client/target/classes\" org.apache.rocketmq.broker.BrokerStartup -n localhost:9876 -c distribution/conf/broker.conf &"
echo ""

# 等待用户确认
read -p "按回车继续运行演示程序..."

echo "启动演示程序..."
mvn exec:java -Dexec.mainClass="org.apache.rocketmq.debug.SimpleAutoDemo" -q

