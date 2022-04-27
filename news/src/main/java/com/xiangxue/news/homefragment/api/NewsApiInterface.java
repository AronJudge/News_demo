package com.xiangxue.news.homefragment.api;

import retrofit2.http.GET;
import retrofit2.http.Query;
import io.reactivex.Observable;

 
public interface NewsApiInterface {
    @GET("release/news")
    Observable<NewsListBean> getNewsList(@Query("channelId") String channelId,
                                         @Query("channelName") String channelName,
                                         @Query("page") String page);

    @GET("release/channel")
    Observable<NewsChannelsBean> getNewsChannels();
}
