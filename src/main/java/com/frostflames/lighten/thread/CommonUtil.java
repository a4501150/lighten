package com.frostflames.lighten.thread;

import java.util.concurrent.TimeUnit;

public class CommonUtil {
    /**
     * This method simulate c++ sleep() call, reduce boilerplate code.
     * Beware this is EXACTLY used for sleep no other goals.
     *
     * @param time time that needs to be sleep
     * @param unit
     */
    public static void sleep(long time, TimeUnit unit)
    {
        try {
            unit.sleep(time);
        } catch (InterruptedException ex)
        {
            Thread.currentThread().interrupt();
            System.err.println("Sleep was interrupted, check your code invoke this method");
        }
    }
}
