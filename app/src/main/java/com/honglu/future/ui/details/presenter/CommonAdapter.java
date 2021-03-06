package com.honglu.future.ui.details.presenter;

import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

public abstract class CommonAdapter<T> extends BaseAdapter {

    protected List<T> mData = new ArrayList<>();

    @Override
    public int getCount() {
        return mData == null ? 0 : mData.size();
    }

    @Override
    public T getItem(int position) {
        return mData == null || position >= mData.size() ? null : mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void updateItem(T item){
//        if(mData.contains(item)){
//            int index = mData.indexOf(item);
//            mData.set(index,item);
//        }
        for(int i=0;i<mData.size();i++){
            if(mData.get(i).equals(item)){
                mData.set(i,item);
                notifyDataSetChanged();
                break;
            }
        }
    }
    public void clearDatas(){
        if (mData != null){
            mData.clear();
            notifyDataSetChanged();
        }
    }

    public void setDatas(List<T> items){
        if (!items.isEmpty() && items != null) {
            mData = items;
        }
        notifyDataSetChanged();
    }

    public void refreshDatas(List<T> items){
        if (!items.isEmpty() && items != null) {
            mData.clear();
            mData.addAll(items);
            notifyDataSetChanged();
        }
    }


    public void loadMoreDatas(List<T> items){
        if (!items.isEmpty() && items != null) {
            mData.addAll(items);
            notifyDataSetChanged();
        }
    }

    public void addDataToFront(List<T> items) {
        if (!items.isEmpty() && items != null) {
            for (T t : items) {
                mData.add(0,t);
            }
            notifyDataSetChanged();
        }
    }

}