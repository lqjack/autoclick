package org.base.autoclick.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by liu on 2017/5/4.
 */
public class MyStack {
    private List<String> list = new ArrayList<String>();

    public static void main(String[] args) {
        MyStack stack = new MyStack();
        new Thread(() -> {
            while (true)
                stack.push("a");
        }).start();
        new Thread(() -> {
            try {
                while (true)
                    stack.pop();
            } catch (InterruptedException e) {
                System.out.println(e.getCause());
                e.printStackTrace();
            }
        }).start();
    }

    public synchronized void push(String value) {
        synchronized (this) {
            list.add(value);
            notify();
        }
    }

    public synchronized String pop() throws InterruptedException {
        synchronized (this) {
            if (list.size() <= 0) {
                wait();
            }
            return list.remove(list.size() - 1);
        }
    }
}