package com.honglu.future.dialog.klineposition;

import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.config.Constant;
import com.honglu.future.ui.trade.bean.HoldPositionBean;
import com.honglu.future.ui.trade.bean.ProductListBean;
import com.honglu.future.util.TradeUtil;
import com.honglu.future.widget.recycler.BaseRecyclerAdapter;
import java.util.List;

/**
 * Created by zq on 2017/11/2.
 */

public class KLinePositionDialogAdapter extends BaseRecyclerAdapter<KLinePositionDialogAdapter.ViewHolder, HoldPositionBean> {
    private ProductListBean mProductListBean;
    private KLinePositionDialogPresenter mPresenter;
    private KLinePositionDialog mDialog;

    private int mPosition = -1;
    private int mExpcNum = 0; //平仓手数
    private int mMaxCloseTradeNum;//最大平仓手数
    private double mExPrice = 0;//价格
    private boolean mEtHasFocus = false;

    private double mLowerLimitPrice; //跌停板价
    private double mUpperLimitPrice; //涨停板价
    private KLinePositionDialogAdapter.ViewHolder mViewHolder;

    public KLinePositionDialogAdapter(KLinePositionDialogPresenter mPresenter, KLinePositionDialog mDialog) {
        this.mPresenter = mPresenter;
        this.mDialog = mDialog;
    }

    public void setProductListBean(ProductListBean bean) {
        this.mExpcNum = 0;
        this.mExPrice = 0;
        this.mProductListBean = bean;
        this.mLowerLimitPrice = getDouble(mProductListBean.getLowerLimitPrice());
        this.mUpperLimitPrice = getDouble(mProductListBean.getUpperLimitPrice());
        notifyDataSetChanged();
    }

    /**
     * mpush 刷新对应数据
     *
     * @param lowerLimitPrice
     * @param upperLimitPrice
     */
    public void setMpushRefreshData(String lowerLimitPrice, String upperLimitPrice,String lastPrice) {
        this.mLowerLimitPrice = Double.parseDouble(lowerLimitPrice);
        this.mUpperLimitPrice = Double.parseDouble(upperLimitPrice);
        if (mViewHolder !=null && mPosition !=-1){
            mViewHolder.mPriceHint.setText("≥" + mLowerLimitPrice + " 跌停价 且 ≤" + mUpperLimitPrice + "涨停价");
        }
        if (!mEtHasFocus){
            mViewHolder.mEtPrice.setText(lastPrice);
        }
    }

    //当前展开的 position
    public int getMPosition() {
        return mPosition;
    }

    //重置 position
    public void clearPosition() {
        this.mPosition = -1;
    }

    //手数
    public int getExpcNum() {
        return mExpcNum;
    }

    //价格
    public double getExPrice() {
        return mExPrice;
    }

    //跌停板价
    public double getLowerLimitPrice(){
        return mLowerLimitPrice;
    }

    //涨停板价
    public double getUpperLimitPrice(){
        return mUpperLimitPrice;
    }

    //是否刷新mpush 消息
    public boolean isMpushRefresh() {

        return mProductListBean != null && mPosition != -1;
    }

    //重置数据
    public void resetData() {
        this.mProductListBean = null;
        this.mExpcNum = 0;
        this.mExPrice = 0;
        clearPosition();
    }

    public void notifyDataChanged(List<HoldPositionBean> list) {
        getData().clear();
        if (list != null) {
            addData(list);
        }
    }


    @Override
    public KLinePositionDialogAdapter.ViewHolder mOnCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_kline_position_dialog, parent, false);
        return new KLinePositionDialogAdapter.ViewHolder(view);
    }

    @Override
    public void mOnBindViewHolder(KLinePositionDialogAdapter.ViewHolder holder, int position) {
        if (position == getItemCount() - 1) {
            holder.mLine.setVisibility(View.INVISIBLE);
        } else {
            holder.mLine.setVisibility(View.VISIBLE);
        }

        //1 跌  2涨
        if (item.getType() == Constant.TYPE_BUY_DOWN) {
            holder.mBuyNum.setTextColor(mContext.getResources().getColor(R.color.color_2CC593));
            holder.mBuyNum.setText("买跌" + item.getPosition() + "手");

        } else {
            holder.mBuyNum.setTextColor(mContext.getResources().getColor(R.color.color_FB4F4F));
            holder.mBuyNum.setText("买涨" + item.getPosition() + "手");
        }

        //持仓均价
        holder.mPriced.setText(item.getHoldAvgPrice());

        //持仓盈亏
        holder.mProfitLoss.setText(item.getTotalProfit());
        if (item.getTotalProfit().indexOf("-") != -1) {
            holder.mProfitLoss.setTextColor(mContext.getResources().getColor(R.color.color_2CC593));
        } else {
            holder.mProfitLoss.setTextColor(mContext.getResources().getColor(R.color.color_FB4F4F));
        }

        mViewHolder = holder;
        final HoldPositionBean mBean = item;

        if (position == mPosition) {
            holder.mGouxuan.setSelected(true);
            holder.mGouxuan.setEnabled(false);
            holder.mLayoutContent.setVisibility(View.VISIBLE);
            holder.mEtMaxpc.setEnabled(false);
            holder.mEtPrice.setFocusableInTouchMode(true);

            if (mProductListBean !=null) {
                //获取最大平仓手数
                mMaxCloseTradeNum = mBean.getPosition();
                //设置全局手数
                this.mExpcNum = mMaxCloseTradeNum;

                holder.mMaxpcNum.setText("平仓手数(最多" + mMaxCloseTradeNum + "手)");

                //最大输入平仓手数
                holder.mEtMaxpc.setText(String.valueOf(mMaxCloseTradeNum));

                //设置手数事件
                setPositionListener(mViewHolder, mBean);

                double mLastPrice = getDouble(mProductListBean.getLastPrice());
                this.mExPrice = mLastPrice;
                holder.mEtPrice.setText(String.valueOf(mExPrice));
                setPriceListener(mViewHolder,mBean);

                holder.mPriceHint.setText("≥" + mLowerLimitPrice + " 跌停价 且 ≤" + mUpperLimitPrice + "涨停价");

                setTextViewData(mViewHolder,mBean,mExPrice,mMaxCloseTradeNum);
            }
        } else {
            holder.mGouxuan.setSelected(false);
            holder.mGouxuan.setEnabled(true);
            holder.mEtMaxpc.setEnabled(false);
            holder.mLayoutContent.setVisibility(View.GONE);
            final int mZPosition = position;
            holder.mGouxuan.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mViewHolder.mEtPrice.clearFocus();
                    mViewHolder.mEtPrice.setFocusable(false);
                    mPosition = mZPosition;
                    mPresenter.getProductDetail(mBean.getInstrumentId());
                }
            });
        }
    }


    private void setTextViewData(KLinePositionDialogAdapter.ViewHolder mHolder,HoldPositionBean mBean ,double mPrice ,int mPcNum){
        if (mPrice == 0){
            mHolder.mYkprice.setText("---");
            mHolder.mSxprice.setText("---");
            mDialog.setPingcangSatte(mBean.getType(), 0);
        }else {
            //实际盈亏
            mHolder.mYkprice.setText("￥" + getActualProfitLoss(mPrice,mPcNum,mBean));
            //平仓手续费
            mHolder.mSxprice.setText("￥" + getCloseTradePrice(mPrice,mPcNum, mBean));
            //平仓盈亏
            mDialog.setPingcangSatte(mBean.getType(), getCloseProfitLoss(mPrice,mPcNum, mBean));
        }
    }


    //手数
    private void setPositionListener(final KLinePositionDialogAdapter.ViewHolder mHolder, final HoldPositionBean mBean) {

        //手数 -
        mHolder.mMaxpcDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int textNum = getIntText(mHolder.mEtMaxpc);
                if (textNum > 1) {
                    textNum--;
                    mHolder.mEtMaxpc.setText(String.valueOf(textNum));
                }
            }
        });

        mHolder.mMaxpcAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int textNum = getIntText(mHolder.mEtMaxpc);
                if (textNum < mMaxCloseTradeNum) {
                    textNum++;
                    mHolder.mEtMaxpc.setText(String.valueOf(textNum));
                }
            }
        });

        mHolder.mEtMaxpc.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                mExpcNum = getIntText(mHolder.mEtMaxpc);
                setTextViewData(mHolder,mBean,getDoubleText(mHolder.mEtPrice),mExpcNum);
            }
        });
    }


    //委托价
    private void setPriceListener(final KLinePositionDialogAdapter.ViewHolder mHolder,final HoldPositionBean mBean) {

        mHolder.mPriceDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double textPrice = getDoubleText(mHolder.mEtPrice);
                if (textPrice - mLowerLimitPrice > 1) {
                    textPrice--;
                    mHolder.mEtPrice.setText(String.valueOf(textPrice));
                }
            }
        });

        mHolder.mPriceAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                double textPrice = getDoubleText(mHolder.mEtPrice);
                if (mUpperLimitPrice - textPrice  > 1) {
                    textPrice++;
                    mHolder.mEtPrice.setText(String.valueOf(textPrice));
                }
            }
        });

        mHolder.mEtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                //跌停板价 mLowerLimitPrice  mUpperLimitPrice; //涨停板价
                mExPrice = getDoubleText(mHolder.mEtPrice);
                mHolder.mEtPrice.setSelection(mHolder.mEtPrice.length());
                setTextViewData(mHolder,mBean,mExPrice,mExpcNum);
            }
        });

        mHolder.mEtPrice.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus){
                    mEtHasFocus = hasFocus;
                }else {
                    mEtHasFocus = hasFocus;
                }
            }
        });
    }

    private int getIntText(EditText mText) {

        return mText.getText() != null && !TextUtils.isEmpty(mText.getText().toString()) ? Integer.parseInt(mText.getText().toString()) : 0;
    }

    private double getDoubleText(EditText mText) {

        return mText.getText() != null && !TextUtils.isEmpty(mText.getText().toString()) ? Double.parseDouble(mText.getText().toString()) : 0;
    }

    private double getDouble(String num){
        if (!TextUtils.isEmpty(num)){
            return Float.parseFloat(num);
        }
        return 0;
    }

    /**
     * 平仓盈亏
     *
     * @param tradeNum 手数
     * @param bean
     * @return
     */
    private double getCloseProfitLoss(double price,int tradeNum, HoldPositionBean bean) {
        String holdAvgPrice = bean.getHoldAvgPrice();
        String priceTick = mProductListBean.getPriceTick();
        int volumeMultiple = mProductListBean.getVolumeMultiple();
        int type = bean.getType();
        try {
           return TradeUtil.getCloseProfitLoss(type,holdAvgPrice,priceTick,volumeMultiple,price,tradeNum);
        }catch (Exception e){
          e.printStackTrace();
            return 0;
        }
    }


    /**
     * 实际盈亏
     *
     * @param tradeNum 手数
     * @param bean
     * @return
     */
    private double getActualProfitLoss(double price,int tradeNum, HoldPositionBean bean) {
        String openAvgPrice = bean.getOpenAvgPrice();
        String priceTick = mProductListBean.getPriceTick();
        int volumeMultiple = mProductListBean.getVolumeMultiple();
        int type = bean.getType();
        try {
            return TradeUtil.getActualProfitLoss(type,openAvgPrice,priceTick,volumeMultiple,price,tradeNum);
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }
    }


    /**
     * 获取平仓手续费
     *
     * @param tradeNum 手数
     * @param bean
     * @return
     */
    private String getCloseTradePrice(double price,int tradeNum, HoldPositionBean bean) {
        String closeTodayRatioByMoney = mProductListBean.getCloseTodayRatioByMoney();
        int volumeMultiple = mProductListBean.getVolumeMultiple();
        String closeTodayRatioByVolume = mProductListBean.getCloseTodayRatioByVolume();
        String closeRatioByMoney = mProductListBean.getCloseRatioByMoney();
        String closeRatioByVolume = mProductListBean.getCloseRatioByVolume();
        int todayPosition = bean.getTodayPosition();
        try {
            double closeTradePrice  = TradeUtil.getCloseTradePrice(todayPosition,closeTodayRatioByMoney,
                    closeTodayRatioByVolume,closeRatioByMoney,closeRatioByVolume,price,tradeNum,volumeMultiple);
            return String.valueOf(closeTradePrice);
        }catch (Exception e){
            e.printStackTrace();
            return "0";
        }
    }


    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView mGouxuan;
        TextView mBuyNum;
        TextView mPriced;
        TextView mProfitLoss;
        ImageView mPriceDel;
        EditText mEtPrice;
        ImageView mPriceAdd;
        TextView mPriceHint;
        TextView mMaxpcNum;
        ImageView mMaxpcDel;
        EditText mEtMaxpc;
        ImageView mMaxpcAdd;
        TextView mSxprice;
        TextView mYkprice;
        LinearLayout mLayoutContent;
        //ExpandableLayout mExpandable;
        View mLine;

        ViewHolder(View view) {
            super(view);
            mGouxuan = (ImageView) view.findViewById(R.id.iv_gouxuan);
            mBuyNum = (TextView) view.findViewById(R.id.tv_buy_num);
            mPriced = (TextView) view.findViewById(R.id.tv_priced);
            mProfitLoss = (TextView) view.findViewById(R.id.tv_profit_loss);
            mPriceDel = (ImageView) view.findViewById(R.id.iv_price_del);
            mEtPrice = (EditText) view.findViewById(R.id.et_price);
            mPriceAdd = (ImageView) view.findViewById(R.id.iv_price_add);
            mPriceHint = (TextView) view.findViewById(R.id.tv_price_hint);
            mMaxpcNum = (TextView) view.findViewById(R.id.tv_maxpc_num);
            mMaxpcDel = (ImageView) view.findViewById(R.id.iv_maxpc_del);
            mEtMaxpc = (EditText) view.findViewById(R.id.et_maxpc);
            mMaxpcAdd = (ImageView) view.findViewById(R.id.iv_maxpc_add);
            mSxprice = (TextView) view.findViewById(R.id.tv_sxprice);
            mYkprice = (TextView) view.findViewById(R.id.tv_ykprice);
            mLayoutContent = (LinearLayout) view.findViewById(R.id.layout_content);
            // mExpandable = (ExpandableLayout) view.findViewById(R.id.el_expandable);
            mLine = view.findViewById(R.id.v_line);
        }
    }
}
