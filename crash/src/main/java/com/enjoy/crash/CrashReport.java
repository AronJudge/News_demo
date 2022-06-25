package com.enjoy.crash;


import android.content.Context;

import androidx.annotation.CallSuper;
import androidx.annotation.UiThread;

import java.io.File;

public class CrashReport {

    static {
        System.loadLibrary("bugly");
    }


    public static void init(Context context) {
        Context applicationContext = context.getApplicationContext();
        CrashHandler.init(applicationContext);
        File file = new File(context.getExternalCacheDir(), "native_crash");
        if (!file.exists()) {
            file.mkdirs();
        }
        initBreakpad(file.getAbsolutePath());
    }

    private static native void initBreakpad(String path);

    public static native void testNativeCrash();

    public static int testJavaCrash() {
        return 1 / 0;
    }

}
