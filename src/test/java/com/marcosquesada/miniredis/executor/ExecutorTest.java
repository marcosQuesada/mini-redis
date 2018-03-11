package com.marcosquesada.miniredis.executor;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorTest {

    @Test
    public void FireExecutorAndWaitResult() {
        Executor e = new Executor("test", 1);
        Callable task = new Callable() {
            @Override
            public Object call() throws Exception {
                return "FOOBAR";
            }
        };
        String res = e.executeAndWait(task);

        Assert.assertTrue(res.equals("FOOBAR"));
    }

    @Test
    public void ScheduleExecutorAndCheckIsDone() {
        AtomicInteger counter = new AtomicInteger();

        Executor e = new Executor("test", 1);
        Runnable task = new Runnable() {
            @Override
            public void run() {
                counter.incrementAndGet();
            }
        };
        ScheduledFuture<?> future =  e.schedule(task, 10L);
        while (!future.isDone()){
            try {
                Thread.sleep(100L);
            }catch (Exception ex) {
                Assert.fail();
            }
        }

        Assert.assertEquals(counter.get(), 1);
    }
}
