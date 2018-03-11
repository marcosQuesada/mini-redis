package com.marcosquesada.miniredis.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Executor {
    private static final Logger logger = LoggerFactory.getLogger(Executor.class);

    private ScheduledThreadPoolExecutor executor;

    public Executor(String schedulerName, Integer poolSize) {
        executor = new ScheduledThreadPoolExecutor(poolSize, new ThreadFactory() {
            private AtomicInteger counter = new AtomicInteger();

            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread(r, String.format("%s_%d", schedulerName, counter.incrementAndGet()));
                t.setDaemon(true);

                return t;
            }
        });
    }

    public String executeAndWait(Callable task) {
        Future<String> futRes = execute(task);
        try {
            return futRes.get();
        }catch (Exception e) {
            if (e.getMessage().contains("or out of range")) {
                String[] parts = e.getMessage().split(":");
                return String.format("(error) ERR %s\n", parts[1]);
            }

            logger.error("Unexpected exception waiting response, err {}", e.getMessage());
            return "(nil)\n";
        }
    }

    public Future<String> execute(Callable task) {
        return executor.submit(task);
    }

    // Schedule Task to be executed in X miliseconds
    public ScheduledFuture<?> schedule(Runnable task, Long delay) {
        return executor.schedule(task, delay, TimeUnit.MILLISECONDS);
    }

}
