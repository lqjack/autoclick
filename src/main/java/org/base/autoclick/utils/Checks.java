package org.base.autoclick.utils;

/**
 * Created by liu on 2017/5/5.
 */
public class Checks {

    public static void checkEmpyt(Object obj) {
        if (obj == null) throw new NullPointerException("obj cannot be null");
    }

    public static void checkEmpty(Object obj, String description) {
        checkEmpyt(description);
        if (obj == null) throw new NullPointerException(description);
    }
}
