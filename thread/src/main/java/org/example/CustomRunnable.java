package org.example;

public class CustomRunnable implements Runnable {

    private final String threadName;

    public CustomRunnable(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public void run() {
        for (int i = 0; i < 10; i++) {
            System.out.println(threadName + " :: " + i);
        }
    }
}
