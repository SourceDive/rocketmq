#!/bin/bash

# RocketMQ 调试模块运行脚本

echo "=== RocketMQ 调试模块运行脚本 ==="
echo ""

# 检查是否在正确的目录
if [ ! -f "pom.xml" ]; then
    echo "错误: 请在 my-debug-module 目录下运行此脚本"
    exit 1
fi

# 编译项目
echo "正在编译项目..."
mvn clean compile -q
if [ $? -ne 0 ]; then
    echo "编译失败，请检查错误信息"
    exit 1
fi
echo "编译成功！"
echo ""

# 显示菜单
while true; do
    echo "请选择要运行的程序:"
    echo "1. 持续生产者 (交互式发送消息)"
    echo "2. 持续消费者 (监听消息)"
    echo "3. 简单生产者 (单次发送)"
    echo "4. 简单消费者 (监听60秒)"
    echo "5. 运行测试"
    echo "6. 退出"
    echo ""
    read -p "请输入选择 (1-6): " choice
    
    case $choice in
        1)
            echo "启动持续生产者..."
            echo "提示: 输入 'auto' 开启自动发送模式，输入 'quit' 退出"
            mvn exec:java -Dexec.mainClass="org.apache.rocketmq.debug.ContinuousProducerExample" -q
            ;;
        2)
            echo "启动持续消费者..."
            echo "提示: 按 Ctrl+C 停止消费者"
            mvn exec:java -Dexec.mainClass="org.apache.rocketmq.debug.ContinuousConsumerExample" -q
            ;;
        3)
            echo "启动简单生产者..."
            mvn exec:java -Dexec.mainClass="org.apache.rocketmq.debug.SimpleProducerExample" -q
            ;;
        4)
            echo "启动简单消费者..."
            mvn exec:java -Dexec.mainClass="org.apache.rocketmq.debug.SimpleConsumerExample" -q
            ;;
        5)
            echo "运行测试..."
            mvn test -q
            ;;
        6)
            echo "退出"
            break
            ;;
        *)
            echo "无效选择，请重新输入"
            ;;
    esac
    echo ""
done
