package com.company.seacher.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Helper class for managing threads
 */
public class ThreadManager<T extends Runnable> {
    private Supplier<T> supplier;
    private String name;
    private List<Thread> threads;

    public ThreadManager(Supplier<T> supplier, String name) {
        this.supplier = supplier;
        this.name = name;
        this.threads = new ArrayList<>();
    }

    /**
     * Starts a fixed number of threads
     *
     * @param  count number of threads to start
     */
    public void launch(int count) {
        for(int i = 1; i <= count; i++) {
            Thread t = new Thread(supplier.get(), name + i);
            threads.add(t);
            t.start();
        }
    }

    /**
     * Waits for all the threads to return
     *
     * @return InterruptedException
     */
    public void join() throws InterruptedException {
        for(Thread t : threads) {
            t.join();
        }
    }
}
