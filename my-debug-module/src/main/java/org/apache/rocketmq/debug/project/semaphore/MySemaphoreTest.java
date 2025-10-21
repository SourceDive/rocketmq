package org.apache.rocketmq.debug.project.semaphore;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

/**
 * @author zero
 * @description 测试信号量
 * @date 2025-10-21
 */
public class MySemaphoreTest {
    public static void main(String[] args) {
        Semaphore semaphore = new Semaphore(1);

        CountDownLatch latch = new CountDownLatch(100);

        Thread[] threads = new Thread[100];
        CountDownLatch startLatch = new CountDownLatch(1);

        // 使用一个原子变量来暂存/统计当前并发数量
        java.util.concurrent.atomic.AtomicInteger currentConcurrency = new java.util.concurrent.atomic.AtomicInteger(0);

        final long[] startTime = {0};
        final long[] endTime = {0};

        for (int i = 0; i < 100; i++) {
            threads[i] = new Thread(() -> {
                try {
                    startLatch.await(); // 等待统一开始信号

                    // 只记录第一个开始工作的线程的开始时间
                    synchronized (startTime) {
                        if (startTime[0] == 0) {
                            startTime[0] = System.currentTimeMillis();
                        }
                    }

                    semaphore.acquire();
                    int running = currentConcurrency.incrementAndGet();
                    System.out.println(Thread.currentThread().getName() + " acquired a permit. Available permits: " + semaphore.availablePermits()
                            + ", 当前并发数量: " + running);
                    // 模拟业务处理
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    int running = currentConcurrency.decrementAndGet();
                    semaphore.release();
                    System.out.println(Thread.currentThread().getName() + " released a permit. Available permits: " + semaphore.availablePermits()
                            + ", 当前并发数量: " + running);

                    // 只记录最后一个线程完成的时间
                    if (latch.getCount() == 1) {
                        endTime[0] = System.currentTimeMillis();
                        System.out.println("总耗时: " + (endTime[0] - startTime[0]) + " ms");
                    }
                    latch.countDown();
                }
            }, "Thread-" + i);
        }

        // 先启动全部线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 发令枪，同时放行所有线程
        startLatch.countDown(); // 初始为1,这里执行完毕后为0,触发所有线程执行。

        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("All threads finished.");
    }

}
