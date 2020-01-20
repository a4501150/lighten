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

    @Test
    void testSleepThrowException() {
        // Setup
        Thread test = new Thread(() -> {
            long currentTime = System.currentTimeMillis();
            CommonUtil.sleep(3000L, TimeUnit.MILLISECONDS);
            long after = System.currentTimeMillis();
            // Verify the results
            Assertions.assertTrue(after - currentTime <= 500);
        });

        test.start();
        test.interrupt();
        Assertions.assertTrue(test.isInterrupted());

    }
}
