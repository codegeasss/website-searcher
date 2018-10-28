package main.java.seacher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ThreadManager<T extends Runnable> {
    private Supplier<T> supplier;
    private String name;
    private List<Thread> threads;

    public ThreadManager(Supplier<T> supplier, String name) {
        this.supplier = supplier;
        this.name = name;
        this.threads = new ArrayList<>();
    }

    public void launch(int count) {
        for(int i = 1; i <= count; i++) {
            Thread t = new Thread(supplier.get(), name + i);
            threads.add(t);
            t.start();
        }
    }

    public void join() throws InterruptedException {
        for(Thread t : threads) {
            t.join();
        }
    }
}
