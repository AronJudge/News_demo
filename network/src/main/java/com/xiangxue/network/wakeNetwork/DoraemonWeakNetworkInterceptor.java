package com.xiangxue.network.wakeNetwork;

import android.annotation.SuppressLint;
import android.util.Log;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class DoraemonWeakNetworkInterceptor implements Interceptor {
    private static final String TAG = "Lance";

    @Override
    public Response intercept(Chain chain) throws IOException {
        if (!WeakNetworkManager.get().isActive()) {
            Request request = chain.request();
            return chain.proceed(request);
        }
        final int type = WeakNetworkManager.get().getType();
        switch (type) {
            case WeakNetworkManager.TYPE_TIMEOUT:
                //超时
                Log.i(TAG, "intercept: 超时模拟");
                return WeakNetworkManager.get().simulateTimeOut(chain);
            case WeakNetworkManager.TYPE_SPEED_LIMIT:
                //限速
                Log.i(TAG, "intercept: 弱网模拟");
                return WeakNetworkManager.get().simulateSpeedLimit(chain);
            default:
                //断网
                Log.i(TAG, "intercept: 断网模拟");
                return WeakNetworkManager.get().simulateOffNetwork(chain);
        }
    }
}