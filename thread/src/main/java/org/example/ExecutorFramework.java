package org.example;

import java.util.ArrayList;
import java.util.concurrent.*;

public class ExecutorFramework {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        ExecutorService executors = Executors.newFixedThreadPool(3);
//
//        for(int i=0;i<10;i++) {
//            int x = i;
//            Future<Integer> future = executors.submit(() -> {
//                System.out.println("Executing task :: " + x + " Thread ::" + Thread.currentThread().getName());
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    throw new RuntimeException(e);
//                }
//                return x;
//            });
//
////            int data = future.get();
////            System.out.println("After processing got data :: " + data);
//        }
//        executors.shutdown();
//        System.out.println("Is terminated :: " + executors.isTerminated());

        ExecutorService ex = Executors.newFixedThreadPool(2);

        Future<String> future = ex.submit(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("TASK DONE !!!!");
            } catch (InterruptedException e) {
                System.out.println(e);
                //throw new RuntimeException(e);
            }
            return "Hello";
        });

        Thread.sleep(1000);
        future.cancel(true);
        System.out.println(future.isCancelled());
        System.out.println(future.isDone());

        ex.shutdown();

    }
}
