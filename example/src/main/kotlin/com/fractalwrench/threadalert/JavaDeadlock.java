package com.fractalwrench.threadalert;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class JavaDeadlock {

    private Lock lock = new ReentrantLock();

    void hangForever() {
        lock.lock();
    }

}
