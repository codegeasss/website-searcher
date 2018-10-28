package com.company.seacher;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Thread safe bounded queue implementation using LinkedList
 * This is my custom implementation for the assignment
 * In practice we could use one of the Java <code>BlockingQueue</code> implementations
 */
class BlockingQueue<E> {
    private Queue<E> queue;
    int max;
    private AtomicBoolean terminate;

    public BlockingQueue(int max) {
        queue = new LinkedList<>();
        this.max = max;
        terminate = new AtomicBoolean(false);
    }

    /**
     * Adds an event to queue
     * Blocks until there is space available if the queue is of max capacity
     */
    public synchronized void put(E event) throws InterruptedException {
        while (queue.size() == max) {
            wait(5000);
        }
        queue.add(event);
        notifyAll();
    }

    /**
     * Removes an event from queue
     * Blocks until there is event available if the queue is empty
     * Exits if there is nothing to consume and a termination signal is received
     */
    public synchronized E take() throws InterruptedException {
        while (queue.isEmpty())   {
            // exit on termination signal. no more events to consume
            if(terminate.get()) {
                System.out.println( String.format("%s terminated!", Thread.currentThread().getName()) );
                return null;
            }
            wait(5000);
        }

        E e = queue.remove();
        notifyAll();
        return e;
    }

    /**
     * Sets the terminate flag
     */
    public void terminate() {
        terminate.set(true);
    }

}
