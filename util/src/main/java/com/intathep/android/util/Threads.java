package com.intathep.android.util;

import android.os.Looper;

public class Threads {

    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }
}
