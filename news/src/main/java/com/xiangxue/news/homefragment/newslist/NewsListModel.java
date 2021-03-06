package com.xiangxue.news.homefragment.newslist;

import com.arch.demo.common.views.picturetitleview.PictureTitleViewViewModel;
import com.arch.demo.common.views.titleview.TitleViewViewModel;
import com.arch.demo.core.customview.BaseCustomViewModel;
import com.xiangxue.network.TecentNetworkApi;
import com.arch.demo.core.model.MvvmBaseModel;
import com.xiangxue.network.TecentNetworkApi;
import com.xiangxue.network.observer.BaseObserver;

import java.util.ArrayList;
import com.xiangxue.news.homefragment.api.NewsListBean;
import com.xiangxue.news.homefragment.api.NewsApiInterface;

 
public class NewsListModel extends MvvmBaseModel<NewsListBean, ArrayList<BaseCustomViewModel>> {
    private String mChannelId = "";
    private String mChannelName = "";

    public NewsListModel(String channelId, String channelName) {
        super(true, "pref_key_news_" + channelId, null, 1);
        mChannelId = channelId;
        mChannelName = channelName;
    }

    @Override
    public void load() {
        TecentNetworkApi.getService(NewsApiInterface.class)
                .getNewsList(mChannelId, mChannelName, String.valueOf(pageNumber))
                .compose(TecentNetworkApi.
                        getInstance().
                        applySchedulers(new BaseObserver<NewsListBean>(this,
                                this)));
    }

    @Override
    public void onSuccess(NewsListBean newsListBean, boolean isFromCache) {
        ArrayList<BaseCustomViewModel> baseViewModels = new ArrayList<>();
        for (NewsListBean.Contentlist source : newsListBean.showapiResBody.pagebean.contentlist) {
            if (source.imageurls != null && source.imageurls.size() > 1) {
                PictureTitleViewViewModel viewModel = new PictureTitleViewViewModel();
                viewModel.avatarUrl = source.imageurls.get(0).url;
                viewModel.jumpUri = source.link;
                viewModel.title = source.title;
                baseViewModels.add(viewModel);
            } else {
                TitleViewViewModel viewModel = new TitleViewViewModel();
                viewModel.jumpUri = source.link;
                viewModel.title = source.title;
                baseViewModels.add(viewModel);
            }
        }
        notifyResultToListeners(newsListBean, baseViewModels, isFromCache);
    }

    @Override
    public void onFailure(Throwable e) {
        e.printStackTrace();

        loadFail(e.getMessage());
    }
}
