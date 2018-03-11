package com.marcosquesada.miniredis.service;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SetTest {

    @Test
    public void SetCompleteWorkFLow() {
        Set set = new Set();
        int a = set.sadd("foo");
        Assert.assertEquals(1, a);

        a = set.sadd("bar");
        Assert.assertEquals(1, a);

        a = set.sadd("fooBar");
        Assert.assertEquals(1, a);

        a = set.sadd("zzz");
        Assert.assertEquals(1, a);

        // On already exist element no entry is done
        a = set.sadd("zzz");
        Assert.assertEquals(0, a);

        Assert.assertTrue(set.scard().equals(4));

        Set set2 = new Set();

        set2.sadd( "foo");
        set2.sadd("bar");
        set2.sadd( "fooBar");
        set2.sadd("xxxxx");

        List<String> inter = Set.sinter(set.getSet(), set2.getSet());

        Assert.assertEquals(inter.size(), 3);

        a = set.srem("zzz");
        Assert.assertEquals(1, a);

        Assert.assertTrue(set.scard().equals(3));
        Assert.assertTrue(set2.scard().equals(4));

        List<String> items = set.smembers();
        Assert.assertEquals(items.size(), 3);

    }

}
