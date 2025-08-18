package org.example;

public class CustomThread extends Thread {

    @Override
    public void run() {
        super.run();
        for (int i = 0; i < 10; i++) {
            System.out.println(currentThread().getName() + " :: " + currentThread().getPriority() + " :: " + i);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread customThread = new CustomThread();
        customThread.setPriority(Thread.NORM_PRIORITY);
        customThread.start();
        //customThread.join(); //wait for customThread to be finished
        //System.out.println("Ended :: " + customThread.getName());
        Thread customThread2 = new CustomThread();
        customThread2.setPriority(Thread.MAX_PRIORITY);
        customThread2.start();

    }
}
