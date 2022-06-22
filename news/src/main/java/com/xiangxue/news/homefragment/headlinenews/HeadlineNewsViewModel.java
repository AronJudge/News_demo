package com.xiangxue.news.homefragment.headlinenews;

import android.os.Looper;
import android.os.MessageQueue;

import com.arch.demo.core.viewmodel.MvvmBaseViewModel;

 
public class HeadlineNewsViewModel extends MvvmBaseViewModel<ChannelsModel, ChannelsModel.Channel> {
    public HeadlineNewsViewModel() {
        model = new ChannelsModel();
        model.register(this);

        Looper.myQueue().addIdleHandler(new MessageQueue.IdleHandler() {
            @Override
            public boolean queueIdle() {
                model.getCachedDataAndLoad();
                return false;
            }
        });
    }

}
