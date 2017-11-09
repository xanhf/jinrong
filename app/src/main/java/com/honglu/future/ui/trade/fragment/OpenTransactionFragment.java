package com.honglu.future.ui.trade.fragment;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.gson.Gson;
import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.base.BaseFragment;
import com.honglu.future.config.Constant;
import com.honglu.future.dialog.AlertFragmentDialog;
import com.honglu.future.events.ReceiverMarketMessageEvent;
import com.honglu.future.mpush.MPushUtil;
import com.honglu.future.ui.home.bean.MarketData;
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
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.ViewUtil;
import com.honglu.future.widget.AmountView;
import com.honglu.future.widget.popupwind.AccountLoginPopupView;
import com.honglu.future.widget.popupwind.BottomPopupWindow;
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

public class OpenTransactionFragment extends BaseFragment<OpenTransactionPresenter> implements OpenTransactionContract.View, AccountContract.View, OpenTransactionAdapter.OnRiseDownClickListener {
    @BindView(R.id.rv_open_transaction_list_view)
    RecyclerView mOpenTransactionListView;
    @BindView(R.id.srl_refreshView)
    SmartRefreshLayout mSmartRefreshLayout;
    private LinearLayout mTradeHeader;
    private ImageView mTradeTip;
    private OpenTransactionAdapter mOpenTransactionAdapter;
    private BottomPopupWindow mPopupWindow;
    private BottomPopupWindow mTipPopupWindow;
    private static final String TRADE_BUY_RISE = "TRADE_BUY_RISE";
    private static final String TRADE_BUY_DOWN = "TRADE_BUY_DOWN";
    private AccountPresenter mAccountPresenter;
    private AccountLoginPopupView mAccountLoginPopupView;
    private String mToken;
    private EditText mHands, mPrice;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            mPresenter.querySettlementInfo(SpUtil.getString(Constant.CACHE_TAG_UID), mToken, "GUOFU");
        }
    };

    public static void tipClickCallBack(OnTipClickCallback onTipClickCallback) {
        onTipClickCallback.tipClick();
    }

    @Override
    public void loginSuccess(AccountBean bean) {
        mAccountLoginPopupView.dismissLoginAccountView();
        SpUtil.putString(Constant.CACHE_ACCOUNT_TOKEN, bean.getToken());
        mHandler.postDelayed(mRunnable, 3000);
        mToken = bean.getToken();
    }

    @Override
    public void querySettlementSuccess(SettlementInfoBean bean) {
        if (bean == null) {
            return;
        }
        SpUtil.putString(Constant.CACHE_ACCOUNT_TOKEN, "");
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
        if (MPushUtil.CODES_TRADE_HOME == null|| !MPushUtil.CODES_TRADE_HOME.equals(MPushUtil.requestCodes)||isHidden()){
            return;
        }
        List<ProductListBean> data = mOpenTransactionAdapter.getData();
        int index = -1;
        RequestMarketMessage marketMessage = event.marketMessage;
        for (int i = 0;i<data.size();i++){
            if (data.get(i).getInstrumentId().equals(marketMessage.getInstrumentID())){
                index = i;
                break;
            }
        }
        if (index>0){
            ProductListBean productListBean = data.get(index);
            productListBean.setAskPrice1(marketMessage.getAskPrice1());
            productListBean.setBidPrice1(marketMessage.getBidPrice1());
            productListBean.setTradeVolume(marketMessage.getVolume());
            data.set(index,productListBean);
            mOpenTransactionAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden){
            MPushUtil.pauseRequest();
        }else {
            if (!TextUtils.isEmpty(MPushUtil.CODES_TRADE_HOME)){
                MPushUtil.requestMarket(MPushUtil.CODES_TRADE_HOME);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!TextUtils.isEmpty(MPushUtil.CODES_TRADE_HOME)){
            MPushUtil.requestMarket(MPushUtil.CODES_TRADE_HOME);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MPushUtil.pauseRequest();
    }

    @Override
    public void getProductListSuccess(List<ProductListBean> bean) {
        mSmartRefreshLayout.finishRefresh();
        if (!TextUtils.isEmpty(MPushUtil.CODES_TRADE_HOME)){
            MPushUtil.requestMarket(MPushUtil.CODES_TRADE_HOME);
        }
        mOpenTransactionAdapter.clearData();
        mOpenTransactionAdapter.addData(bean);
    }

    @Override
    public void buildTransactionSuccess() {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            mPopupWindow.dismiss();
        }
    }

    interface OnTipClickCallback {
        void tipClick();
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
                showTipWindow(v);
            }
        });
    }

    private void initData() {
        mPresenter.getProductList();
    }

    private void showBottomWindow(View view, View layout, int flag, ProductListBean bean) {
        if (mPopupWindow != null && mPopupWindow.isShowing()) {
            return;
        }
        mPopupWindow = new BottomPopupWindow(mActivity, view, layout);
        //添加按键事件监听
        setButtonListeners(layout, flag, bean);
        //添加pop窗口关闭事件，主要是实现关闭时改变背景的透明度
        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                ViewUtil.backgroundAlpha(mActivity, 1f);
            }
        });
    }

    private void showTipBottomWindow(View view, View layout, final int flag) {
        if (mTipPopupWindow != null && mTipPopupWindow.isShowing()) {
            return;
        }
        mTipPopupWindow = new BottomPopupWindow(mActivity,
                view, layout);
        //添加按键事件监听
        setButtonListeners(layout, flag, null);
        //添加pop窗口关闭事件，主要是实现关闭时改变背景的透明度
        mTipPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (mPopupWindow == null || !mPopupWindow.isShowing()) {
                    ViewUtil.backgroundAlpha(mActivity, 1f);
                }
            }
        });
    }

    private void setButtonListeners(View view, final int flag, final ProductListBean bean) {
        ImageView ivClose = (ImageView) view.findViewById(R.id.iv_close_popup);
        ivClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (flag == 1) {
                    if (mTipPopupWindow != null && mTipPopupWindow.isShowing()) {
                        mTipPopupWindow.dismiss();
                    }
                } else {
                    if (mPopupWindow != null && mPopupWindow.isShowing()) {
                        mPopupWindow.dismiss();
                    }
                }
            }
        });

        if (flag == 2 || flag == 3) {
            ImageView ivTip = (ImageView) view.findViewById(R.id.iv_open_account_tip);
            ivTip.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    tipClickCallBack(new OnTipClickCallback() {
                        @Override
                        public void tipClick() {
                            showTipWindow(mTradeTip);
                        }
                    });
                }
            });

            if (flag == 3) {
                TextView fastOpen = (TextView) view.findViewById(R.id.btn_fast_open);
                fastOpen.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        new AlertFragmentDialog.Builder(mActivity).setTitle("确认建仓").setContent("玉米1080 买涨 1手 总计 ¥2511.68")
                                .setRightBtnText("确定")
                                .setLeftBtnText("取消")
                                .setRightCallBack(new AlertFragmentDialog.RightClickCallBack() {
                                    @Override
                                    public void dialogRightBtnClick(String string) {
                                        // TODO: 2017/11/6 需要动态获取涨跌方向 zq
                                        mPresenter.buildTransaction(mHands.getText().toString(),
                                                "2",
                                                mPrice.getText().toString(),
                                                bean.getInstrumentId(),
                                                SpUtil.getString(Constant.CACHE_TAG_UID),
                                                SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN),
                                                "GUOFU"
                                        );
                                    }
                                }).build();
                    }
                });
            }
        }
    }


    @Override
    public void onRiseClick(View view, ProductListBean bean) {
        if (!App.getConfig().getLoginStatus()) {
            Intent intent = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(intent);
        } else {
            if (App.getConfig().getAccountLoginStatus()) {
                showOpenTransactionWindow(view, TRADE_BUY_RISE, bean);
            } else {
                mAccountLoginPopupView = new AccountLoginPopupView(mActivity, mTradeTip, mAccountPresenter);
                mAccountLoginPopupView.showOpenAccountWindow();
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
                showOpenTransactionWindow(view, TRADE_BUY_DOWN, bean);
            } else {
                mAccountLoginPopupView = new AccountLoginPopupView(mActivity, mTradeTip, mAccountPresenter);
                mAccountLoginPopupView.showOpenAccountWindow();
            }
        }
    }

    @Override
    public void onItemClick(ProductListBean bean) {
        Intent intent = new Intent(mActivity, KLineMarketActivity.class);
        intent.putExtra("excode",bean.getExcode());
        intent.putExtra("code",bean.getInstrumentId());
        intent.putExtra("isClosed",bean.getIsClosed());
        startActivity(intent);
    }


    private void showTipWindow(View view) {
        View layout = LayoutInflater.from(mActivity).inflate(R.layout.layout_trade_tip_pop_window, null);
        showTipBottomWindow(view, layout, 1);
        ViewUtil.backgroundAlpha(mActivity, .5f);
    }

    private void showOpenTransactionWindow(View view, String riseOrDown, ProductListBean bean) {
        View layout = LayoutInflater.from(mActivity).inflate(R.layout.open_transaction_popup_window, null);
        showBottomWindow(view, layout, 3, bean);
        initTransactionData(layout, riseOrDown, bean);
        ViewUtil.backgroundAlpha(mActivity, .5f);
    }

    private void initTransactionData(View view, String riseOrDown, ProductListBean bean) {
        TextView name = (TextView) view.findViewById(R.id.tv_name);
        name.setText(bean.getInstrumentName());
        TextView rise = (TextView) view.findViewById(R.id.tv_rise);
        rise.setText(bean.getLastPrice());
        TextView down = (TextView) view.findViewById(R.id.tv_down);
        down.setText(String.valueOf(Double.valueOf(bean.getLastPrice()) - 1));
        TextView riseRadio = (TextView) view.findViewById(R.id.tv_rise_radio);
        riseRadio.setText(bean.getLongRate());
        TextView downRadio = (TextView) view.findViewById(R.id.tv_down_radio);
        downRadio.setText(bean.getShortRate());
        mPrice = (EditText) view.findViewById(R.id.amountView);
        mPrice.setText(bean.getLastPrice());
        mHands = (EditText) view.findViewById(R.id.av_hands);
        mHands.setText(String.valueOf(bean.getMinSl()));
    }
}
