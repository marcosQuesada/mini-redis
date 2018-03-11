package com.marcosquesada.miniredis.service;

import com.marcosquesada.miniredis.executor.Executor;
import org.junit.Assert;
import org.junit.Test;

public class RedisTest {

    @Test
    public void RedisGetSet() {

        Redis redis = new Redis(null);

        String key = "key1";
        String res = redis.set(key, "FOO");

        String gres = redis.get(key);

        Assert.assertTrue(gres.equals("FOO"));

        Assert.assertTrue(res.equals("OK"));

    }

    @Test
    public void RedisSetWithExpiration() {

        Redis redis = new Redis(new Executor("expirator", 1));

        String key = "key1";
        redis.set(key, "FOO", 1L);

        String gres = redis.get(key);

        Assert.assertTrue(gres.equals("FOO"));

        try{
            Thread.sleep(1500L);

            gres = redis.get(key);

            Assert.assertTrue(gres.equals("(nil)"));
        }catch (Exception e){
            Assert.fail();
        }

    }

    @Test
    public void RedisIncrRString() {

        Redis redis = new Redis(null);

        String key = "key1";
        Integer val = redis.incr(key);

        Assert.assertTrue(val.equals(1));
        redis.incr(key);
        val = redis.incr(key);
        Assert.assertTrue(val.equals(3));
    }


    @Test
    public void RedisIncrRStringButIsNotIntFormat() {

        Redis redis = new Redis(null);

        String key = "key1";
        redis.set(key, "FOO");

        try {
            redis.incr(key);
        }catch (NumberFormatException e) {
            Assert.assertEquals(e.getMessage(), "For input string: \"FOO\"");
        }catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }


}
