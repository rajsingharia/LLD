package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPool {
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(3);

        for(int i=0;i<10;i++) {
            Integer taskNumber = i;
            executor.submit(() -> {
                System.out.println("Task :: " + taskNumber + " By Thread :: " + Thread.currentThread().getName());
            });

            try {
                Thread.sleep(1000); // simulate work
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        executor.shutdown();

    }
}
