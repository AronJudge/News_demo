package com.xiangxue.news.homefragment.headlinenews;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.xiangxue.news.homefragment.newslist.NewsListFragment;

import java.util.List;

 
public class HeadlineNewsFragmentAdapter2 extends FragmentStateAdapter {
    private List<ChannelsModel.Channel> mChannels;
    private int itemCount = 0;

    public HeadlineNewsFragmentAdapter2(@NonNull FragmentManager fragmentManager,
                                        @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }


    public void setChannels(List<ChannelsModel.Channel> channels) {
        this.mChannels = channels;
        itemCount = channels.size();
        notifyDataSetChanged();
    }

    public String getChannelName(int position) {
        if (position >= mChannels.size()) {
            return "";
        }
        return mChannels.get(position).channelName;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return NewsListFragment.newInstance(mChannels.get(position).channelId, mChannels.get(position).channelName);
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }
}