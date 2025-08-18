package org.example;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class PubSub {
    private final Queue<Integer> queue = new LinkedList<>();
    private final Integer arrayMaxSize = 10;

    public synchronized void produce(int value) throws InterruptedException {
        while(this.queue.size() == this.arrayMaxSize) {
            wait();
        }
        queue.add(value);
        System.out.println("Produced: " + value);
        notify();
    }

    public synchronized void consume() throws InterruptedException {
        while(this.queue.size() == 0) {
            wait();
        }
        Integer val = this.queue.poll();
        System.out.println("Consumed: " + val);
        notify();
    }

    public static void main(String[] args) {
        PubSub pubSub = new PubSub();

        Thread t1 = new Thread(() -> {
            for(int i=0;i<100;i++) {
                try {
                    pubSub.produce(i);
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread t2 = new Thread(() -> {
            for(int i=0;i<100;i++) {
                try {
                    pubSub.consume();
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        t1.start();
        t2.start();

    }

}
