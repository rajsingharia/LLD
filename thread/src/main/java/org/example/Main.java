package org.example;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
//        Thread customThread2 = new Thread(new CustomRunnable(Thread.currentThread().getName()));
//        customThread2.start();
//        new Thread(() -> {
//            System.out.println("lambda runnable in :: " + Thread.currentThread().getName());
//        }).start();
        ReEntrantLock reEntrantLock = new ReEntrantLock();
        reEntrantLock.perform();

    }
}