package com.honglu.future.ui.trade.fragment;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.Gson;
import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.base.BaseFragment;
import com.honglu.future.config.Constant;
import com.honglu.future.dialog.AccountLoginDialog;
import com.honglu.future.dialog.AlertFragmentDialog;
import com.honglu.future.dialog.BuildTransactionDialog;
import com.honglu.future.dialog.TradeTipDialog;
import com.honglu.future.events.ReceiverMarketMessageEvent;
import com.honglu.future.events.RefreshUIEvent;
import com.honglu.future.events.UIBaseEvent;
import com.honglu.future.mpush.MPushUtil;
import com.honglu.future.ui.login.activity.LoginActivity;
import com.honglu.future.ui.main.contract.AccountContract;
import com.honglu.future.ui.main.presenter.AccountPresenter;
import com.honglu.future.ui.trade.adapter.OpenTransactionAdapter;
import com.honglu.future.ui.trade.bean.AccountBean;
import com.honglu.future.ui.trade.bean.ProductListBean;
import com.honglu.future.ui.trade.bean.SettlementInfoBean;
import com.honglu.future.ui.trade.billconfirm.BillConfirmActivity;
import com.honglu.future.ui.trade.contract.OpenTransactionContract;
import com.honglu.future.ui.trade.kchart.KLineMarketActivity;
import com.honglu.future.ui.trade.presenter.OpenTransactionPresenter;
import com.honglu.future.ui.usercenter.activity.UserAccountActivity;
import com.honglu.future.ui.usercenter.bean.AccountInfoBean;
import com.honglu.future.util.NumberUtils;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.ViewUtil;
import com.honglu.future.widget.recycler.DividerItemDecoration;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;
import com.xulu.mpush.message.RequestMarketMessage;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import butterknife.BindView;

import static com.honglu.future.util.ToastUtil.showToast;

/**
 * Created by zq on 2017/10/26.
 */

public class OpenTransactionFragment extends BaseFragment<OpenTransactionPresenter> implements OpenTransactionContract.View,
        AccountContract.View, OpenTransactionAdapter.OnRiseDownClickListener {
    @BindView(R.id.rv_open_transaction_list_view)
    RecyclerView mOpenTransactionListView;
    @BindView(R.id.srl_refreshView)
    SmartRefreshLayout mSmartRefreshLayout;
    private LinearLayout mTradeHeader;
    private ImageView mTradeTip;
    private OpenTransactionAdapter mOpenTransactionAdapter;

    private AccountPresenter mAccountPresenter;
    private AccountLoginDialog mAccountLoginDialog;
    private BuildTransactionDialog mBuildTransactionDialog;
    private String mToken;
    private String mSelectCode;

    @Override
    public void loginSuccess(AccountBean bean) {
        mToken = bean.getToken();
        mAccountLoginDialog.dismiss();
        SpUtil.putString(Constant.CACHE_ACCOUNT_TOKEN, bean.getToken());
        startRun();
    }

    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            getAccountBasicInfo();//每隔3秒刷一次
            mHandler.postDelayed(this, 3000);
        }
    };

    private void getAccountBasicInfo() {
        mPresenter.getAccountInfo(SpUtil.getString(Constant.CACHE_TAG_UID), SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN), "GUOFU");
    }

    /**
     * 开始刷新用户信息
     */
    public void startRun() {
        if (App.getConfig().getAccountLoginStatus()) {
            mHandler.removeCallbacks(mRunnable);
            mHandler.post(mRunnable);
        }
    }

    public void stopRun() {
        mHandler.removeCallbacks(mRunnable);
    }

    @Override
    public void querySettlementSuccess(SettlementInfoBean bean) {
        if (bean == null) {
            return;
        }
        //SpUtil.putString(Constant.CACHE_ACCOUNT_TOKEN, "");
        Intent intent = new Intent(mActivity, BillConfirmActivity.class);
        intent.putExtra("SettlementBean", new Gson().toJson(bean));
        intent.putExtra("token", mToken);
        startActivity(intent);
    }

    /*******
     * 将事件交给事件派发controller处理
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReceiverMarketMessageEvent event) {
        if (MPushUtil.CODES_TRADE_HOME == null || !MPushUtil.CODES_TRADE_HOME.equals(MPushUtil.requestCodes) || isHidden()) {
            return;
        }
        List<ProductListBean> data = mOpenTransactionAdapter.getData();
        int index = -1;
        RequestMarketMessage marketMessage = event.marketMessage;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).getInstrumentId().equals(marketMessage.getInstrumentID())) {
                index = i;
                break;
            }
        }
        ProductListBean productListBean = null;
        if (index > 0) {
            productListBean = data.get(index);
            productListBean.setAskPrice1(marketMessage.getAskPrice1());
            productListBean.setBidPrice1(marketMessage.getBidPrice1());
            productListBean.setTradeVolume(marketMessage.getVolume());
            data.set(index, productListBean);
            mOpenTransactionAdapter.notifyDataSetChanged();
        }

        if (mBuildTransactionDialog != null
                && mBuildTransactionDialog.isShowing()
                && !TextUtils.isEmpty(mSelectCode) && mSelectCode.equals(marketMessage.getInstrumentID())) {
            mBuildTransactionDialog.pushRefresh(productListBean);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden) {
            stopRun();
            MPushUtil.pauseRequest();
        } else {
            if (!App.getConfig().getAccountLoginStatus()) {
                stopRun();
            } else {
                startRun();
            }
            if (!TextUtils.isEmpty(MPushUtil.CODES_TRADE_HOME)) {
                MPushUtil.requestMarket(MPushUtil.CODES_TRADE_HOME);
            }

            if (TextUtils.isEmpty(SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN)) || TextUtils.isEmpty(SpUtil.getString(Constant.CACHE_TAG_UID))) {
                return;
            }
//            mPresenter.querySettlementInfo(SpUtil.getString(Constant.CACHE_TAG_UID), SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN), "GUOFU");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (TextUtils.isEmpty(SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN)) || TextUtils.isEmpty(SpUtil.getString(Constant.CACHE_TAG_UID))) {
            return;
        }
        //mPresenter.querySettlementInfo(SpUtil.getString(Constant.CACHE_TAG_UID), SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN), "GUOFU");
    }

    @Override
    public void getProductListSuccess(List<ProductListBean> bean) {
        mSmartRefreshLayout.finishRefresh();
        if (!TextUtils.isEmpty(MPushUtil.CODES_TRADE_HOME)) {
            MPushUtil.requestMarket(MPushUtil.CODES_TRADE_HOME);
        }
        mOpenTransactionAdapter.clearData();
        mOpenTransactionAdapter.addData(bean);
    }

    private TextView mDangerChance;
    private TextView mRightsInterests;
    private TextView mMoney;
    private TextView mProfitLoss;

    @Override
    public void getAccountInfoSuccess(AccountInfoBean bean) {
        mDangerChance.setText(bean.getCapitalProportion());
        mRightsInterests.setText(NumberUtils.formatFloatNumber(bean.getRightsInterests()));
        mMoney.setText(NumberUtils.formatFloatNumber(bean.getAvailable()));
        mProfitLoss.setText(bean.getPositionProfit() + "");
    }


    /***********
     * eventBus 监听
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(UIBaseEvent event) {
        if (event instanceof RefreshUIEvent) {
            int code = ((RefreshUIEvent) event).getType();
            if (code == UIBaseEvent.EVENT_ACCOUNT_LOGOUT) {//安全退出期货账户
                if (!App.getConfig().getAccountLoginStatus()) {
                    stopRun();
                    mDangerChance.setText("--");
                    mRightsInterests.setText("--");
                    mMoney.setText("--");
                    mProfitLoss.setText("--");
                }
            }
        }
    }

    @Override
    public void showLoading(String content) {
        if (!TextUtils.isEmpty(content)) {
            App.loadingContent(mActivity, content);
        }
    }

    @Override
    public void stopLoading() {
        App.hideLoading();
    }

    @Override
    public void showErrorMsg(String msg, String type) {
        showToast(msg);
    }


    @Override
    public int getLayoutId() {
        return R.layout.fragment_open_transaction;
    }

    @Override
    public void initPresenter() {
        mPresenter.init(this);
        mAccountPresenter = new AccountPresenter();
        mAccountPresenter.init(this);
    }

    @Override
    public void loadData() {
        EventBus.getDefault().register(this);
        initView();
        initData();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        mOpenTransactionListView.setLayoutManager(new LinearLayoutManager(mContext));
        mOpenTransactionListView.addItemDecoration(new DividerItemDecoration(mContext, DividerItemDecoration.VERTICAL_LIST));
        mOpenTransactionAdapter = new OpenTransactionAdapter();
        View headView = LayoutInflater.from(mActivity).inflate(R.layout.item_trade_list_header, null);

        //剩余平分
        LinearLayout rootLayoutLeft = (LinearLayout) headView.findViewById(R.id.rootLayout_left);
        int screenWidth = ViewUtil.getScreenWidth(getActivity());
        int pixelSize_15 = getResources().getDimensionPixelSize(R.dimen.dimen_15dp);
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) rootLayoutLeft.getLayoutParams();
        params.width = (screenWidth - pixelSize_15 * 4) / 2;
        rootLayoutLeft.setLayoutParams(params);

        mDangerChance = (TextView) headView.findViewById(R.id.tv_danger_chance);
        mRightsInterests = (TextView) headView.findViewById(R.id.tv_rights_interests);
        mMoney = (TextView) headView.findViewById(R.id.tv_money);
        mProfitLoss = (TextView) headView.findViewById(R.id.tv_profit_loss);
        mTradeHeader = (LinearLayout) headView.findViewById(R.id.ll_trade_header);
        mTradeTip = (ImageView) headView.findViewById(R.id.iv_trade_tip);
        mOpenTransactionAdapter.addHeaderView(headView);
        mOpenTransactionListView.setAdapter(mOpenTransactionAdapter);
        setListener();
    }

    private void setListener() {
        mOpenTransactionAdapter.setOnRiseDownClickListener(this);

        mTradeHeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!App.getConfig().getLoginStatus()) {
                    Intent intent = new Intent(mActivity, LoginActivity.class);
                    startActivity(intent);
                } else {
                    if (!App.getConfig().getAccountLoginStatus()) {
                        mAccountLoginDialog = new AccountLoginDialog(mContext, mAccountPresenter);
                        mAccountLoginDialog.show();
                    } else {
                        startActivity(UserAccountActivity.class);
                    }
                }
            }
        });
        mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                MPushUtil.pauseRequest();
                mPresenter.getProductList();
            }
        });
        mTradeTip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TradeTipDialog tradeTipDialog = new TradeTipDialog(mContext, R.layout.layout_trade_tip_pop_window);
                tradeTipDialog.show();
            }
        });
    }

    private void initData() {
        mPresenter.getProductList();
    }

    @Override
    public void onRiseClick(View view, ProductListBean bean) {
        if (!App.getConfig().getLoginStatus()) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
        } else {
            if (App.getConfig().getAccountLoginStatus()) {
                mSelectCode = bean.getInstrumentId();
                mBuildTransactionDialog = new BuildTransactionDialog(mContext, BuildTransactionDialog.TRADE_BUY_RISE, bean.getInstrumentId());
                mBuildTransactionDialog.show();
            } else {
                mAccountLoginDialog = new AccountLoginDialog(mActivity, mAccountPresenter);
                mAccountLoginDialog.show();
            }
        }
    }

    @Override
    public void onDownClick(View view, ProductListBean bean) {
        if (!App.getConfig().getLoginStatus()) {
            new AlertFragmentDialog.Builder(mActivity).setContent("请登陆后再操作")
                    .setRightBtnText("确定")
                    .setLeftBtnText("取消")
                    .setRightCallBack(new AlertFragmentDialog.RightClickCallBack() {
                        @Override
                        public void dialogRightBtnClick(String string) {
                            Intent intent = new Intent(mContext, LoginActivity.class);
                            mContext.startActivity(intent);
                        }
                    }).build();
        } else {
            if (App.getConfig().getAccountLoginStatus()) {
                mSelectCode = bean.getInstrumentId();
                mBuildTransactionDialog = new BuildTransactionDialog(mContext, BuildTransactionDialog.TRADE_BUY_DOWN, bean.getInstrumentId());
                mBuildTransactionDialog.show();
            } else {
                mAccountLoginDialog = new AccountLoginDialog(mActivity, mAccountPresenter);
                mAccountLoginDialog.show();
            }
        }
    }

    @Override
    public void onItemClick(ProductListBean bean) {
        Intent intent = new Intent(mActivity, KLineMarketActivity.class);
        intent.putExtra("excode", bean.getExcode());
        intent.putExtra("code", bean.getInstrumentId());
        intent.putExtra("isClosed", bean.getIsClosed());
        startActivity(intent);
    }

}
