package com.marcosquesada.miniredis.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class ZsetTest {

    @Test
    public void ZsetCompleteWorkFLow() {
        SortedSet zset = new SortedSet("fooKey");

        zset.zadd(100L, "foo");
        zset.zadd(10L, "bar");
        zset.zadd(100L, "fooBar");
        zset.zadd(101L, "zzz");

        Assert.assertTrue(zset.zcard().equals(4));
        Assert.assertTrue(zset.zrank("bar").equals(3));
        Assert.assertTrue(zset.zrank("zzz").equals(0));

        zset.zadd(1000L, "DDz");
        Assert.assertTrue(zset.zrank("DDz").equals(0));
        Assert.assertTrue(zset.zrank("zzz").equals(1));

        List<String> range = zset.zrange(0, 5);
        Assert.assertTrue(range.size() == 5);
        Assert.assertTrue(zset.zcard().equals(5));

        // Update foo score
        zset.zadd(1L, "foo");
        Assert.assertTrue(zset.zrank("foo").equals(4));

        zset.zrem("foo");
        Assert.assertTrue(zset.zcard().equals(4));
    }

    @Test //Pending to complete "ZINTERSTORE out 2 foo bar"
    public void ZsetCompleteZsetInterWorkFLow() {

        SortedSet zset = new SortedSet("fooKey");

        zset.zadd(100L, "foo");
        zset.zadd(10L, "bar");
        zset.zadd(100L, "fooBar");
        zset.zadd(101L, "zzz");

        SortedSet zset2 = new SortedSet("fooKeyB");

        zset2.zadd(10L, "bar");
        zset2.zadd(100L, "fooBar");
        zset2.zadd(101L, "zzz");
        zset2.zadd(11L, "aaa");

        java.util.Set<String> targetSet = new HashSet<>(zset.getZset());
        java.util.Set<String> targetSetB = new HashSet<>(zset2.getZset());

        HashSet set1 = new HashSet(targetSet);
        HashSet set2 = new HashSet(targetSetB);

        List<String> res = Set.sinter(set1, set2);

        Assert.assertEquals(3, res.size());
        Assert.assertTrue(res.get(0).equals("fooBar"));
        Assert.assertTrue(res.get(1).equals("bar"));
        Assert.assertTrue(res.get(2).equals("zzz"));

    }

}