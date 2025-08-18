package org.example;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ReEntrantLock {

    private final Lock lock = new ReentrantLock();

    public void perform() {
        lock.lock();
        System.out.println("outer function done!!!");
        innerPerform();
        lock.unlock();
    }

    private void innerPerform() {
        lock.lock();
        System.out.println("inner function done!!!");
        lock.unlock();
    }

}
