package com.honglu.future.ui.trade.kchart;

import com.honglu.future.base.BaseView;
import com.honglu.future.ui.trade.bean.HoldPositionBean;
import com.honglu.future.ui.trade.bean.ProductListBean;
import com.honglu.future.ui.trade.bean.ProductListBean;
import com.honglu.future.ui.trade.bean.RealTimeBean;

import java.util.List;

/**
 * Created by zq on 2017/11/7.
 */

public interface KLineMarketContract {
    interface View extends BaseView {
        void getProductRealTimeSuccess(RealTimeBean bean);
        void getHoldPositionListSuccess(List<HoldPositionBean> list);
        void getProductDetailSuccess(ProductListBean bean);
    }

    interface Presenter {
        void getProductRealTime(String codes);
        void getHoldPositionList(String userId, String token, String company);
        void getProductDetail(String instrumentId);
    }
}
