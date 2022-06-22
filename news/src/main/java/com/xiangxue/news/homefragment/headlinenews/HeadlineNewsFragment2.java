package com.xiangxue.news.homefragment.headlinenews;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.xiangxue.news.R;
import com.xiangxue.news.databinding.FragmentHome2Binding;

import java.util.List;

public class HeadlineNewsFragment2 extends Fragment {
    public HeadlineNewsFragmentAdapter2 adapter;
    FragmentHome2Binding viewDataBinding;
    HeadlineNewsViewModel viewModel;
    private TabLayoutMediator tabLayoutMediator;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewDataBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_home2, container, false);
        adapter = new HeadlineNewsFragmentAdapter2(getChildFragmentManager(), getLifecycle());
        viewDataBinding.tablayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        viewDataBinding.viewpager2.setAdapter(adapter);

        tabLayoutMediator = new TabLayoutMediator(viewDataBinding.tablayout,
                viewDataBinding.viewpager2,  new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(adapter.getChannelName(position));
            }
        });
        tabLayoutMediator.attach();

        viewDataBinding.viewpager2.setOffscreenPageLimit(1);

        viewModel = new ViewModelProvider(getActivity())
                .get(HeadlineNewsViewModel.class);

        viewModel.dataList.observe(this, new Observer<List<ChannelsModel.Channel>>() {
            @Override
            public void onChanged(List<ChannelsModel.Channel> channels) {
                adapter.setChannels(channels);
                tabLayoutMediator.detach();
                tabLayoutMediator.attach();
            }
        });

        return viewDataBinding.getRoot();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tabLayoutMediator.detach();
    }
}
