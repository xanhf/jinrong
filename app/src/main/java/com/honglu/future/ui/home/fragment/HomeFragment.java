package com.honglu.future.ui.home.fragment;

import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.honglu.future.R;
import com.honglu.future.base.BaseFragment;
import com.honglu.future.config.Constant;
import com.honglu.future.events.HomeNotifyRefreshEvent;
import com.honglu.future.events.LoginEvent;
import com.honglu.future.events.ReceiverMarketMessageEvent;
import com.honglu.future.events.RefreshUIEvent;
import com.honglu.future.events.UIBaseEvent;
import com.honglu.future.http.HttpManager;
import com.honglu.future.http.HttpSubscriber;
import com.honglu.future.http.RxHelper;
import com.honglu.future.mpush.MPushUtil;
import com.honglu.future.ui.home.bean.MarketData;
import com.honglu.future.ui.home.viewmodel.BannerViewModel;
import com.honglu.future.ui.home.viewmodel.HomeBottomTabViewModel;
import com.honglu.future.ui.home.viewmodel.HomeMarketPriceViewModel;
import com.honglu.future.ui.home.viewmodel.HorizontalIconViewModel;
import com.honglu.future.util.SpUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnLoadmoreListener;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;

/**********
 * 首页
 */
public class HomeFragment extends BaseFragment {
    private static final String TAG = "HomeFragment";
    @BindView(R.id.home_scroll_view)
    LinearLayout mScrollView; //跟布局
    @BindView(R.id.home_smart_view)
    SmartRefreshLayout mSmartRefreshView; //刷新
    public static HomeFragment homeFragment;
    private BannerViewModel bannerViewModel;

    public static HomeFragment getInstance() {
        if (homeFragment == null) {
            homeFragment = new HomeFragment();
        }
        return homeFragment;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_home_layout;
    }

    @Override
    public void initPresenter() {
    }

    @Override
    public void loadData() {
        EventBus.getDefault().register(this);
        initView();
    }

    /*******
     * 将事件交给事件派发controller处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(ReceiverMarketMessageEvent event) {
        if (homeMarketPriceViewModel != null
                && MPushUtil.requestCodes.equals(homeMarketPriceViewModel.productList)
                && !(isHidden())) {
            MarketData.MarketDataBean dataBean = new MarketData.MarketDataBean();
            dataBean.instrumentID = event.marketMessage.getInstrumentID();
            dataBean.change = event.marketMessage.getChange();
            dataBean.chg = event.marketMessage.getChg();
            dataBean.lastPrice = event.marketMessage.getLastPrice();
            homeMarketPriceViewModel.refreshPrice(dataBean);
        }
    }

    /*******
     * 将事件交给事件派发controller处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(LoginEvent event) {
        if (homeBottomTabViewModel != null) {
            homeBottomTabViewModel.refreshData();
        }
    }

    /*******
     * 将事件交给事件派发controller处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(RefreshUIEvent event) {
        if (event.getType() == UIBaseEvent.EVENT_CIRCLE_MSG_RED_VISIBILITY) {//显示
            bannerViewModel.v_red.setVisibility(View.VISIBLE);
        } else if (event.getType() == UIBaseEvent.EVENT_CIRCLE_MSG_RED_GONE) {//点击
            getMsgRed();
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged: ");
        if (hidden) {
            MPushUtil.pauseRequest();
        } else {
            getMsgRed();
            if (homeMarketPriceViewModel != null) {
                homeMarketPriceViewModel.refreshData();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()){
            Log.d(TAG, "onResume: ");
            getMsgRed();
            if (homeMarketPriceViewModel != null && !isHidden()) {
                homeMarketPriceViewModel.refreshData();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MPushUtil.pauseRequest();
    }

    /*******
     * 刷新完成
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(HomeNotifyRefreshEvent event) {
        if (event.isLoadMore()) {
            mSmartRefreshView.finishLoadmore();
        } else if (event.isRefresh()) {
            mSmartRefreshView.finishRefresh();
        }
    }

    private HomeBottomTabViewModel homeBottomTabViewModel;
    private HomeMarketPriceViewModel homeMarketPriceViewModel;

    private void initView() {
        bannerViewModel = new BannerViewModel(getContext());
        homeMarketPriceViewModel = new HomeMarketPriceViewModel(getContext());
        HorizontalIconViewModel horizontalIconViewModel = new HorizontalIconViewModel(getContext());
        homeBottomTabViewModel = new HomeBottomTabViewModel(getContext(), mSmartRefreshView);
        mScrollView.addView(bannerViewModel.mView);//添加banner
        mScrollView.addView(homeMarketPriceViewModel.mView);//添加banner
        mScrollView.addView(horizontalIconViewModel.mView);//添加banner
        mScrollView.addView(homeBottomTabViewModel.mView);//添加banner
        mSmartRefreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getMsgRed();
                homeBottomTabViewModel.refreshData();
                homeMarketPriceViewModel.refreshData();
            }
        });
        mSmartRefreshView.setOnLoadmoreListener(new OnLoadmoreListener() {
            @Override
            public void onLoadmore(RefreshLayout refreshlayout) {
                homeBottomTabViewModel.loadMore();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        homeFragment = null;
    }

    private void getMsgRed() {
        HttpManager.getApi().getMsgRed(SpUtil.getString(Constant.CACHE_TAG_UID)).compose(RxHelper.<Boolean>handleSimplyResult()).subscribe(new HttpSubscriber<Boolean>() {
            @Override
            protected void _onNext(Boolean aBoolean) {
                super._onNext(aBoolean);
                if (bannerViewModel != null) {
                    bannerViewModel.v_red.setVisibility(aBoolean ? View.VISIBLE : View.GONE);
                }
            }

            @Override
            protected void _onError(String message) {
                super._onError(message);
                if (bannerViewModel != null) {
                    bannerViewModel.v_red.setVisibility(View.GONE);
                }
            }
        });
    }
}
