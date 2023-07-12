package com.paddi.service.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @description 共享线程池
 * @author chackylee
 * @param
 * @return
*/
@Service
@Slf4j
public class SharedThreadPool {


    private final ThreadPoolExecutor threadPoolExecutor;

    {
        final AtomicInteger counter = new AtomicInteger(0);

        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 120, TimeUnit.SECONDS, new LinkedBlockingQueue<>(2 << 20), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r);
                t.setDaemon(true);
                t.setName("shared-thread-" + counter.getAndIncrement());
                return t;
            }
        });

    }


    private AtomicLong counter = new AtomicLong(0);

    public void submit(Runnable runnable) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();

        counter.incrementAndGet();

        threadPoolExecutor.submit(() -> {
            long start = System.currentTimeMillis();
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("shared-thread-pool executed task error [{}]", e);
            } finally {
                long end = System.currentTimeMillis();
                long duration = end - start;
                long andDecrement = counter.decrementAndGet();
                if (duration > 1000) {
                    log.warn("shared-thread-pool executed task done,remanent num = {},slow task fatal warning,costs time = {},stack: {}", andDecrement, duration, stackTrace);
                } else if (duration > 300) {
                    log.warn("shared-thread-pool executed task done,remanent num = {},slow task warning: {},costs time = {},", andDecrement,runnable, duration);
                } else {
                    log.debug("shared-thread-pool executed task done,remanent num = {}", andDecrement);
                }
            }
        });


    }

}
