package com.xiangxue.news.homefragment.newslist;

import androidx.hilt.Assisted;
import androidx.hilt.lifecycle.ViewModelInject;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.arch.demo.core.customview.BaseCustomViewModel;
import com.arch.demo.core.viewmodel.MvvmBaseViewModel;

import static com.xiangxue.news.homefragment.newslist.NewsListFragment.BUNDLE_KEY_PARAM_CHANNEL_ID;
import static com.xiangxue.news.homefragment.newslist.NewsListFragment.BUNDLE_KEY_PARAM_CHANNEL_NAME;

 
public class NewsListViewModel extends MvvmBaseViewModel<NewsListModel, BaseCustomViewModel> {
    @ViewModelInject
    public NewsListViewModel(@Assisted SavedStateHandle savedStateHandle) {
        model = new NewsListModel(savedStateHandle.get(BUNDLE_KEY_PARAM_CHANNEL_ID), savedStateHandle.get(BUNDLE_KEY_PARAM_CHANNEL_NAME));
        model.register(this);
    }
}
