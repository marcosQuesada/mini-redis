package com.marcosquesada.miniredis.service;

import org.junit.Assert;
import org.junit.Test;

public class RStringTest {

    @Test
    public void RStringDevelopmentFlow() {
        RString r = new RString("foo");

        Assert.assertTrue(r.getValue().equals("foo"));

    }

    @Test
    public void RStringThrowsExceptionOnIncrementString() {
        RString r = new RString("foo");

        try {
            r.incr();
            Assert.fail();
        } catch (NumberFormatException e) {
            Assert.assertTrue(e.getMessage().equals("For input string: \"foo\""));
        } catch (Exception e) {
            Assert.fail();
        }
    }

}