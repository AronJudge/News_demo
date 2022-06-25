package com.enjoy.crash;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;


import com.tencent.mars.xlog.Log;
import com.tencent.mars.xlog.Xlog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class CrashHandler implements Thread.UncaughtExceptionHandler {
    private static final String FILE_NAME_SUFFIX = ".trace";
    private static Thread.UncaughtExceptionHandler defaultUncaughtExceptionHandler;
    private static Context context;


    public static void init(Context applicationContext) {
        context = applicationContext;
        defaultUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(new CrashHandler());
    }

    @Override
    public void uncaughtException(@NonNull Thread t, @NonNull Throwable e) {
        try {
            File file = dealException(t, e);

        } catch (Exception exception) {

        } finally {
            if (defaultUncaughtExceptionHandler != null) {
                defaultUncaughtExceptionHandler.uncaughtException(t, e);
            }
        }
    }

    private File dealException(Thread thread, Throwable throwable) throws JSONException, IOException, PackageManager.NameNotFoundException {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        //私有目录，无需权限
        File f = new File(context.getExternalCacheDir().getAbsoluteFile(), "crash_info");
        if (!f.exists()) {
            f.mkdirs();
        }
        File crashFile = new File(f, time + FILE_NAME_SUFFIX);
        PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(crashFile)));
        pw.println(time);
        pw.println("Thread: " + thread.getName());
        pw.println(getPhoneInfo());
        throwable.printStackTrace(pw); //写入crash堆栈
        pw.flush();
        pw.close();
        return crashFile;
    }

    private String getPhoneInfo() throws PackageManager.NameNotFoundException {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);
        StringBuilder sb = new StringBuilder();
        //App版本
        sb.append("App Version: ");
        sb.append(pi.versionName);
        sb.append("_");
        sb.append(pi.versionCode + "\n");

        //Android版本号
        sb.append("OS Version: ");
        sb.append(Build.VERSION.RELEASE);
        sb.append("_");
        sb.append(Build.VERSION.SDK_INT + "\n");

        //手机制造商
        sb.append("Vendor: ");
        sb.append(Build.MANUFACTURER + "\n");

        //手机型号
        sb.append("Model: ");
        sb.append(Build.MODEL + "\n");

        //CPU架构
        sb.append("CPU: ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            sb.append(Arrays.toString(Build.SUPPORTED_ABIS));
        } else {
            sb.append(Build.CPU_ABI);
        }
        return sb.toString();
    }
}
