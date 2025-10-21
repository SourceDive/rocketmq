package org.apache.rocketmq.debug.project.semaphore;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author zero
 * @description 信号量测试案例 - 学习RocketMQ中信号量的使用原理
 * @date 2025-10-21
 */
public class SemaphoreTest {

    /**
     * 模拟RocketMQ的SemaphoreReleaseOnlyOnce类
     */
    static class SemaphoreReleaseOnlyOnce {
        private final AtomicBoolean released = new AtomicBoolean(false);
        private final Semaphore semaphore;

        public SemaphoreReleaseOnlyOnce(Semaphore semaphore) {
            this.semaphore = semaphore;
        }

        public void release() {
            if (this.semaphore != null) {
                if (this.released.compareAndSet(false, true)) {
                    this.semaphore.release();
                    System.out.println("信号量已释放，当前可用许可数: " + semaphore.availablePermits());
                } else {
                    System.out.println("信号量已经被释放过了，无法重复释放");
                }
            }
        }

        public Semaphore getSemaphore() {
            return semaphore;
        }
    }

    /**
     * 模拟RocketMQ的异步请求处理
     */
    static class AsyncRequestProcessor {
        private final Semaphore semaphore;
        private final AtomicInteger requestCount = new AtomicInteger(0);
        private final AtomicInteger successCount = new AtomicInteger(0);
        private final AtomicInteger rejectCount = new AtomicInteger(0);

        public AsyncRequestProcessor(int permits) {
            this.semaphore = new Semaphore(permits, true); // 公平信号量
        }

        /**
         * 模拟异步请求处理 - 对应RocketMQ的invokeAsyncImpl方法
         */
        public void processAsyncRequest(String requestId, long timeoutMillis) {
            int currentRequestId = requestCount.incrementAndGet();
            System.out.println("收到异步请求: " + requestId + " (第" + currentRequestId + "个请求)");
            
            try {
                // 尝试获取信号量，设置超时时间
                boolean acquired = semaphore.tryAcquire(timeoutMillis, TimeUnit.MILLISECONDS);
                
                if (acquired) {
                    // 创建只能释放一次的包装器
                    SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(semaphore);
                    
                    System.out.println("请求 " + requestId + " 获取信号量成功，当前可用许可数: " + semaphore.availablePermits());
                    
                    // 模拟异步处理
                    CompletableFuture.runAsync(() -> {
                        try {
                            // 模拟业务处理时间
                            Thread.sleep(1000 + (int)(Math.random() * 2000));
                            System.out.println("请求 " + requestId + " 处理完成");
                            successCount.incrementAndGet();
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        } finally {
                            // 确保释放信号量
                            once.release();
                        }
                    });
                    
                } else {
                    // 获取信号量失败，模拟流控
                    System.out.println("请求 " + requestId + " 获取信号量失败，触发流控");
                    rejectCount.incrementAndGet();
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("请求 " + requestId + " 被中断");
            }
        }

        public void printStats() {
            System.out.println("=== 请求统计 ===");
            System.out.println("总请求数: " + requestCount.get());
            System.out.println("成功处理数: " + successCount.get());
            System.out.println("被拒绝数: " + rejectCount.get());
            System.out.println("当前可用许可数: " + semaphore.availablePermits());
            System.out.println("等待队列长度: " + semaphore.getQueueLength());
        }
    }

    /**
     * 基础信号量测试
     */
    public static void testBasicSemaphore() {
        System.out.println("\n=== 基础信号量测试 ===");
        
        // 创建只有2个许可的信号量
        Semaphore semaphore = new Semaphore(2);
        
        System.out.println("初始可用许可数: " + semaphore.availablePermits());
        
        try {
            // 获取第一个许可
            semaphore.acquire();
            System.out.println("获取第一个许可后，可用许可数: " + semaphore.availablePermits());
            
            // 获取第二个许可
            semaphore.acquire();
            System.out.println("获取第二个许可后，可用许可数: " + semaphore.availablePermits());
            
            // 尝试获取第三个许可（应该失败）
            boolean acquired = semaphore.tryAcquire(1, TimeUnit.SECONDS);
            System.out.println("尝试获取第三个许可: " + acquired);
            
            // 释放一个许可
            semaphore.release();
            System.out.println("释放一个许可后，可用许可数: " + semaphore.availablePermits());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 异步信号量测试 - 模拟RocketMQ场景
     */
    public static void testAsyncSemaphore() {
        System.out.println("\n=== 异步信号量测试 ===");
        
        // 创建只有3个许可的异步请求处理器
        AsyncRequestProcessor processor = new AsyncRequestProcessor(3);
        
        // 模拟10个并发请求
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 1; i <= 10; i++) {
            final int requestId = i;
            executor.submit(() -> {
                processor.processAsyncRequest("Request-" + requestId, 2000);
            });
        }
        
        // 等待一段时间让请求处理
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        processor.printStats();
        executor.shutdown();
    }

    /**
     * 流控测试 - 演示信号量如何控制并发
     */
    public static void testFlowControl() {
        System.out.println("\n=== 流控测试 ===");
        
        // 创建只有2个许可的信号量
        Semaphore semaphore = new Semaphore(2);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger rejectCount = new AtomicInteger(0);
        
        // 创建10个线程同时尝试获取信号量
        ExecutorService executor = Executors.newFixedThreadPool(10);
        
        for (int i = 1; i <= 10; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    boolean acquired = semaphore.tryAcquire(100, TimeUnit.MILLISECONDS);
                    if (acquired) {
                        System.out.println("线程 " + threadId + " 获取信号量成功");
                        successCount.incrementAndGet();
                        
                        // 模拟处理时间
                        Thread.sleep(2000);
                        
                        // 释放信号量
                        semaphore.release();
                        System.out.println("线程 " + threadId + " 释放信号量");
                    } else {
                        System.out.println("线程 " + threadId + " 获取信号量失败，被流控");
                        rejectCount.incrementAndGet();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        
        // 等待所有线程完成
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        System.out.println("流控测试结果:");
        System.out.println("成功获取信号量: " + successCount.get());
        System.out.println("被流控拒绝: " + rejectCount.get());
        
        executor.shutdown();
    }

    /**
     * SemaphoreReleaseOnlyOnce测试
     */
    public static void testSemaphoreReleaseOnlyOnce() {
        System.out.println("\n=== SemaphoreReleaseOnlyOnce测试 ===");
        
        Semaphore semaphore = new Semaphore(1);
        SemaphoreReleaseOnlyOnce once = new SemaphoreReleaseOnlyOnce(semaphore);
        
        try {
            // 获取信号量
            semaphore.acquire();
            System.out.println("获取信号量后，可用许可数: " + semaphore.availablePermits());
            
            // 第一次释放
            once.release();
            
            // 尝试第二次释放（应该被忽略）
            once.release();
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) {
        System.out.println("开始信号量测试案例...");
        
        // 运行各种测试
        testBasicSemaphore();
        testAsyncSemaphore();
        testFlowControl();
        testSemaphoreReleaseOnlyOnce();
        
        System.out.println("\n所有测试完成！");
    }
}
