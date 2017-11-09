package com.honglu.future.ui.trade.adapter;

import android.content.Context;
import android.support.annotation.IntegerRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.ui.trade.bean.HistoryBuiderPositionBean;
import com.honglu.future.ui.trade.bean.HistoryClosePositionBean;
import com.honglu.future.ui.trade.bean.HistoryMissPositionBean;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by zhuaibing on 2017/10/26
 */

public class TradeRecordAdapter extends BaseAdapter {

    public static final int TYPE_IS_MISS = 1;
    public static final int TYPE_IS_builder = 2;
    public static final int TYPE_IS_CLOSE = 3;


    private int mType = TYPE_IS_builder;
    private List<HistoryMissPositionBean> mMissList;
    private List<HistoryBuiderPositionBean> mBudierList;
    private List<HistoryClosePositionBean> mCloseList;
    private Context mContext;

    public TradeRecordAdapter(Context context) {
        this.mContext = context;
    }

    public void setMList(List<HistoryMissPositionBean> list){
        mMissList = list;
        mType = TYPE_IS_MISS;
        notifyDataSetChanged();
    }
    public void setCList(List<HistoryClosePositionBean> list){
        mCloseList = list;
        mType = TYPE_IS_CLOSE;
        notifyDataSetChanged();
    }
    public void setBList(List<HistoryBuiderPositionBean> list){
        mBudierList = list;
        mType = TYPE_IS_builder;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        if (mType == TYPE_IS_CLOSE){
            return mCloseList != null ? mCloseList.size() : 0;
        }else if (mType == TYPE_IS_MISS){
            return mMissList != null ? mMissList.size() : 0;
        }else if (mType == TYPE_IS_builder){
            return mBudierList != null ? mBudierList.size() : 0;
        }else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        if (mType == TYPE_IS_CLOSE){
            return mCloseList.get(position);
        }else if (mType == TYPE_IS_MISS){
            return mMissList.get(position);
        }else if (mType == TYPE_IS_builder){
            return mBudierList.get(position);
        }else {
            return null;
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_trade_record_layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Object item = getItem(position);
        if (mType == TYPE_IS_CLOSE){
            holder.bindView((HistoryClosePositionBean) item);
        }else if (mType == TYPE_IS_MISS){
            holder.bindView((HistoryMissPositionBean) item);
        }else if (mType == TYPE_IS_builder){
            holder.bindView((HistoryBuiderPositionBean) item);
        }else {
            return null;
        }
        return convertView;
    }


    static class ViewHolder {
        private Context mContext;
        @BindView(R.id.tv_name)
        TextView tvName;
        @BindView(R.id.tv_purchaseSize)
        TextView tvPurchaseSize;
        @BindView(R.id.tv_ccmoney)
        TextView tvCcmoney;
        @BindView(R.id.tv_newMoney)
        TextView tvNewMoney;
        @BindView(R.id.tv_profitLossMoney)
        TextView tvProfitLossMoney;
        @BindView(R.id.v_line)
        View vLine;
        ViewHolder(View view) {
            mContext = view.getContext();
            ButterKnife.bind(this, view);
        }

        void bindView(HistoryMissPositionBean bean){
            tvCcmoney.setVisibility(View.INVISIBLE);
            tvNewMoney.setVisibility(View.VISIBLE);
            tvProfitLossMoney.setText(bean.price);
            tvName.setText(bean.instrumentName);
            String num;
            if (bean.type == 1){
                tvPurchaseSize.setTextColor(mContext.getResources().getColor(R.color.color_2CC593));
                num =  mContext.getString(R.string.buy_down_num,bean.position);
            }else {
                tvPurchaseSize.setTextColor(mContext.getResources().getColor(R.color.color_FB4F4F));
                num =  mContext.getString(R.string.buy_up_num,bean.position);
            }
            tvPurchaseSize.setText(num);
            if (bean.openClose ==1){
                tvNewMoney.setText("平仓");
            }else {
                tvNewMoney.setText("建仓");
            }
            tvProfitLossMoney.setText(bean.price);
        }
        void bindView(HistoryClosePositionBean bean){
            tvCcmoney.setVisibility(View.VISIBLE);
            tvNewMoney.setVisibility(View.VISIBLE);
            tvName.setText(bean.instrumentName);
           tvCcmoney.setText(bean.price);
            String num;
            if (bean.type == 1){
                tvPurchaseSize.setTextColor(mContext.getResources().getColor(R.color.color_2CC593));
                num =  mContext.getString(R.string.buy_down_num,bean.position);
            }else {
                tvPurchaseSize.setTextColor(mContext.getResources().getColor(R.color.color_FB4F4F));
                num =  mContext.getString(R.string.buy_up_num,bean.position);
            }
            tvPurchaseSize.setText(num);
            tvNewMoney.setText(bean.closePrice);
            tvProfitLossMoney.setText(bean.profitLoss);
        }
        void bindView(HistoryBuiderPositionBean bean){
            tvCcmoney.setVisibility(View.INVISIBLE);
            tvNewMoney.setVisibility(View.INVISIBLE);
            String num;
            if (bean.type == 1){
                tvPurchaseSize.setTextColor(mContext.getResources().getColor(R.color.color_2CC593));
                num =  mContext.getString(R.string.buy_down_num,bean.position);
            }else {
                tvPurchaseSize.setTextColor(mContext.getResources().getColor(R.color.color_FB4F4F));
                num =  mContext.getString(R.string.buy_up_num,bean.position);
            }
            tvPurchaseSize.setText(num);
            tvName.setText(bean.instrumentName);
            tvProfitLossMoney.setText(bean.price);
        }
    }
}
