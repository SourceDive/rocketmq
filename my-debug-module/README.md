# My Debug Module

这是RocketMQ源码学习环境中的调试模块，用于学习和测试RocketMQ的各种功能。

## 模块结构

```
my-debug-module/
├── src/
│   ├── main/java/org/apache/rocketmq/debug/
│   │   ├── SimpleProducerExample.java    # 简单生产者示例
│   │   └── SimpleConsumerExample.java    # 简单消费者示例
│   └── test/java/org/apache/rocketmq/debug/
│       └── DebugModuleTest.java          # 模块测试类
├── pom.xml                               # Maven配置文件
└── README.md                             # 说明文档
```

## 功能特性

- **直接依赖RocketMQ源码模块**：可以直接引用和调试RocketMQ的所有内部组件
- **完整的示例代码**：包含生产者和消费者的基本使用示例
- **测试环境**：提供单元测试来验证模块设置和依赖关系
- **调试友好**：可以设置断点，单步调试RocketMQ源码

## 使用方法

### 1. 编译模块

```bash
# 在RocketMQ根目录下执行
mvn clean compile -pl my-debug-module
```

### 2. 运行测试

```bash
# 运行模块测试
mvn test -pl my-debug-module
```

### 3. 运行示例

```bash
# 运行生产者示例
mvn exec:java -pl my-debug-module -Dexec.mainClass="org.apache.rocketmq.debug.SimpleProducerExample"

# 运行消费者示例
mvn exec:java -pl my-debug-module -Dexec.mainClass="org.apache.rocketmq.debug.SimpleConsumerExample"
```

### 4. 在IDE中调试

1. 导入整个RocketMQ项目到IDE
2. 在 `my-debug-module` 中的示例类设置断点
3. 运行调试模式，可以单步跟踪RocketMQ源码

## 学习建议

1. **从简单示例开始**：先运行 `SimpleProducerExample` 和 `SimpleConsumerExample`
2. **设置断点调试**：在关键方法设置断点，观察RocketMQ内部执行流程
3. **修改源码实验**：可以修改RocketMQ源码，观察行为变化
4. **添加自己的测试**：在 `src/test/java` 目录下添加更多测试用例

## 注意事项

- 运行示例前需要先启动NameServer和Broker
- 默认连接地址为 `localhost:9876`，可根据实际情况修改
- 建议使用IDE进行调试，体验更佳
