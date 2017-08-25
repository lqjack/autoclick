package org.base.autoclick.utils;

/**
 * Created by liu on 2017/5/4.
 */
public class DispatchTest {
    public static void main(String[] args) {
        Base b = new Sub();
        System.out.println(b.x);
    }
}

class Base {
    int x = 10;

    public Base() {
        this.printMessage();
        x = 20;
    }

    public void printMessage() {
        System.out.println("Base.x = " + x);
    }
}

class Sub extends Base {
    int x = 30;

    public Sub() {
        this.printMessage();
        x = 40;
    }

    public void printMessage() {
        System.out.println("Sub.x = " + x);
    }
}