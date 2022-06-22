package com.xiangxue.news.homefragment.newslist;

import androidx.annotation.NonNull;

import android.os.Bundle;

import androidx.lifecycle.SavedStateViewModelFactory;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.arch.demo.core.customview.BaseCustomViewModel;
import com.arch.demo.core.fragment.MvvmFragment;
import com.scwang.smartrefresh.header.WaterDropHeader;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.scwang.smartrefresh.layout.footer.BallPulseFooter;
import com.scwang.smartrefresh.layout.listener.OnLoadMoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xiangxue.news.R;
import com.xiangxue.news.databinding.FragmentNewsBinding;

import java.io.File;
import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

 
@AndroidEntryPoint
public class NewsListFragment extends MvvmFragment<FragmentNewsBinding, NewsListViewModel, BaseCustomViewModel> {
    private NewsListRecyclerViewAdapter mAdapter;

    protected final static String BUNDLE_KEY_PARAM_CHANNEL_ID = "bundle_key_param_channel_id";
    protected final static String BUNDLE_KEY_PARAM_CHANNEL_NAME = "bundle_key_param_channel_name";

    public static NewsListFragment newInstance(String channelId, String channelName) {
        NewsListFragment fragment = new NewsListFragment();
        if (channelName.equalsIgnoreCase("国内焦点"))
            Log.e("国内焦点:", "newInstance..");
        Bundle bundle = new Bundle();
        bundle.putString(BUNDLE_KEY_PARAM_CHANNEL_ID, channelId);
        bundle.putString(BUNDLE_KEY_PARAM_CHANNEL_NAME, channelName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_news;
    }

    @Override
    public NewsListViewModel getViewModel() {
        return new ViewModelProvider(getActivity(),
                new SavedStateViewModelFactory(getActivity().getApplication(),
                        getActivity(), getArguments()))
                .get(getFragmentTag(), NewsListViewModel.class);
    }

    @Override
    public void onListItemInserted(ArrayList<BaseCustomViewModel> sender) {
        mAdapter.setData(sender);
        viewDataBinding.refreshLayout.finishLoadMore();
        viewDataBinding.refreshLayout.finishRefresh();
        showSuccess();
    }

    @Override
    public void onFinishRefresh() {
        viewDataBinding.refreshLayout.finishLoadMore();
        viewDataBinding.refreshLayout.finishRefresh();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mFragmentTag = "NewsListFragment";
        viewDataBinding.listview.setHasFixedSize(true);
        viewDataBinding.listview.setLayoutManager(new LinearLayoutManager(getContext()));
        viewDataBinding.listview.addItemDecoration(new RecycleViewDivider(getContext(),
                LinearLayoutManager.HORIZONTAL));
        mAdapter = new NewsListRecyclerViewAdapter(getActivity());
        viewDataBinding.listview.setAdapter(mAdapter);
        viewDataBinding.refreshLayout.setRefreshHeader(new WaterDropHeader(getContext()));
        viewDataBinding.refreshLayout.setRefreshFooter(new BallPulseFooter(getContext()).setSpinnerStyle(SpinnerStyle.Scale));
        viewModel.dataList.observe(this, this);
        viewDataBinding.refreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {
                viewModel.tryToRefresh();
            }
        });
        viewDataBinding.refreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {
                viewModel.tryToLoadNextPage();
            }
        });
        setLoadSir(viewDataBinding.refreshLayout);
        showLoading();
    }

    /***
     * 重试按钮点击
     */
    protected void onRetryBtnClick() {
        viewModel.tryToRefresh();
    }

    @Override
    protected String getFragmentTag() {
        return getArguments().getString(BUNDLE_KEY_PARAM_CHANNEL_NAME);
    }
}