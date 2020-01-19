package com.frostflames.lighten.thread;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

class CommonUtilTest {

    @Test
    void testSleep() {
        // Setup

        // Run the test
        long currentTime = System.currentTimeMillis();
        CommonUtil.sleep(100L, TimeUnit.MILLISECONDS);
        long after = System.currentTimeMillis();
        // Verify the results
        Assertions.assertTrue(after - currentTime <= 500);
    }
}
