package com.xiangxue.arch_demo.application;

import android.app.Application;

import com.kingja.loadsir.core.LoadSir;
import com.xiangxue.base.BaseApplication;
import com.xiangxue.base.loadsir.CustomCallback;
import com.xiangxue.base.loadsir.EmptyCallback;
import com.xiangxue.base.loadsir.ErrorCallback;
import com.xiangxue.base.loadsir.LoadingCallback;
import com.xiangxue.base.loadsir.TimeoutCallback;
import com.xiangxue.base.preference.BasicDataPreferenceUtil;
import com.xiangxue.base.preference.PreferencesUtil;
import com.xiangxue.network.base.NetworkApi;


public class ArchDemoApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        NetworkApi.init(new NetworkRequestInfo(this));
        PreferencesUtil.init(this);
        LoadSir.beginBuilder()
                .addCallback(new ErrorCallback())//添加各种状态页
                .addCallback(new EmptyCallback())
                .addCallback(new LoadingCallback())
                .addCallback(new TimeoutCallback())
                .addCallback(new CustomCallback())
                .setDefaultCallback(LoadingCallback.class)//设置默认状态页
                .commit();
    }
}
