package org.apache.rocketmq.debug.project.semaphore;

import org.apache.rocketmq.remoting.common.SemaphoreReleaseOnlyOnce;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class PreciseLicenseControlTest {
    private final Semaphore semaphore = new Semaphore(2); // 只有2个许可证
    private final AtomicInteger actualConcurrent = new AtomicInteger(0);
    private final AtomicInteger maxConcurrent = new AtomicInteger(0);
    
    public void testPreciseControl() throws InterruptedException {
        int totalThreads = 10;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(totalThreads);
        
        for (int i = 0; i < totalThreads; i++) {
            new Thread(() -> {
                try {
                    startLatch.await(); // 同时开始
                    
                    boolean acquired = false;
                    SemaphoreReleaseOnlyOnce once = null;
                    
                    try {
                        // 尝试获取许可证
                        acquired = semaphore.tryAcquire(50, TimeUnit.MILLISECONDS);
                        if (acquired) {
                            once = new SemaphoreReleaseOnlyOnce(semaphore);
                            
                            // 记录并发数
                            int current = actualConcurrent.incrementAndGet();
                            maxConcurrent.updateAndGet(prev -> Math.max(prev, current));
                            
                            System.out.println(Thread.currentThread().getName() + 
                                " 进入临界区，当前并发: " + current + 
                                ", 许可证可用: " + semaphore.availablePermits());
                            
                            // 模拟工作
                            Thread.sleep(100);
                            
                            actualConcurrent.decrementAndGet();
                        } else {
                            System.out.println(Thread.currentThread().getName() + " 获取许可证失败");
                        }
                    } finally {
                        if (once != null) {
                            once.release();
                            System.out.println(Thread.currentThread().getName() + 
                                " 释放许可证，现在可用: " + semaphore.availablePermits());
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            }, "Worker-" + i).start();
        }
        
        startLatch.countDown(); // 同时启动所有线程
        endLatch.await();
        
        System.out.println("最大并发数: " + maxConcurrent.get());
        System.out.println("最终许可证可用数: " + semaphore.availablePermits());
    }
    
    public static void main(String[] args) throws InterruptedException {
        new PreciseLicenseControlTest().testPreciseControl();
    }
}