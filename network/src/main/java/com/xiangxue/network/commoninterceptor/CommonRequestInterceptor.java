package com.xiangxue.network.commoninterceptor;

import android.Manifest;
import android.app.Application;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;

import com.xiangxue.network.base.INetworkRequiredInfo;
import com.xiangxue.network.utils.NetWorkUtils;
import com.xiangxue.network.utils.TecentUtil;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class CommonRequestInterceptor implements Interceptor {
    private INetworkRequiredInfo requiredInfo;

    public CommonRequestInterceptor(INetworkRequiredInfo requiredInfo) {
        this.requiredInfo = requiredInfo;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();
        Application context = requiredInfo.getApplicationContext();
        //builder.cacheControl(CacheControl.FORCE_CACHE);
        builder.addHeader("os", "android");
        builder.addHeader("appVersion", this.requiredInfo.getAppVersionCode());
        builder.addHeader("networkType", String.valueOf(NetWorkUtils.getNetworkState(context)));
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            builder.addHeader("networkSignal", String.valueOf(NetWorkUtils.getMobileDbm(context)));
        }

        return chain.proceed(builder.build());
    }
}
