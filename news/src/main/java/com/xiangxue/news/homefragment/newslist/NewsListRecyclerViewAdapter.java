package com.xiangxue.news.homefragment.newslist;

import android.content.Context;
import android.os.Debug;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableList;
import androidx.recyclerview.widget.RecyclerView;

import com.arch.demo.common.views.picturetitleview.PictureTitleView;
import com.arch.demo.common.views.picturetitleview.PictureTitleViewViewModel;
import com.arch.demo.common.views.titleview.TitleView;
import com.arch.demo.common.views.titleview.TitleViewViewModel;
import com.arch.demo.core.customview.BaseCustomViewModel;
import com.arch.demo.core.recyclerview.BaseViewHolder;
import com.xiangxue.news.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;


 
public class NewsListRecyclerViewAdapter extends RecyclerView.Adapter<BaseViewHolder> {

    private Context context;
    private List<BaseCustomViewModel> mItems;
    private final int VIEW_TYPE_PICTURE_TITLE = 1;
    private final int VIEW_TYPE_TITLE = 2;

    NewsListRecyclerViewAdapter(Context context) {
        this.context = context;

    }

    void setData(List<BaseCustomViewModel> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        if (mItems != null && mItems.size() > 0) {
            return mItems.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        if (mItems.get(position) instanceof PictureTitleViewViewModel) {
            return VIEW_TYPE_PICTURE_TITLE;
        } else if (mItems.get(position) instanceof TitleViewViewModel) {
            return VIEW_TYPE_TITLE;
        }
        return -1;
    }

    @Override
    public BaseViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_PICTURE_TITLE) {
            PictureTitleView pictureTitleView = new PictureTitleView(context);
            return new BaseViewHolder(pictureTitleView);
        } else if (viewType == VIEW_TYPE_TITLE) {
            TitleView titleView = new TitleView(context);
            return new BaseViewHolder(titleView);
        }
        return null;
    }


    @Override
    public void onBindViewHolder(BaseViewHolder holder, int position) {
        holder.bind(mItems.get(position));
    }

}
