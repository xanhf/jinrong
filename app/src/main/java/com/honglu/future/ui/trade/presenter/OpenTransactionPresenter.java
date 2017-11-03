package com.honglu.future.ui.trade.presenter;

import com.honglu.future.base.BasePresenter;
import com.honglu.future.http.HttpManager;
import com.honglu.future.http.HttpSubscriber;
import com.honglu.future.ui.trade.bean.AccountBean;
import com.honglu.future.ui.trade.bean.SettlementInfoBean;
import com.honglu.future.ui.trade.contract.OpenTransactionContract;
import com.honglu.future.util.AESUtils;

/**
 * Created by zq on 2017/10/26.
 */

public class OpenTransactionPresenter extends BasePresenter<OpenTransactionContract.View> implements OpenTransactionContract.Presenter {
    @Override
    public void login(String account, String password, String userId, String company) {
        toSubscribe(HttpManager.getApi().loginAccount(account, AESUtils.encrypt(password), userId, company), new HttpSubscriber<AccountBean>() {
            @Override
            public void _onStart() {
                mView.showLoading("登录中...");
            }

            @Override
            protected void _onNext(AccountBean bean) {
                mView.loginSuccess(bean);
            }

            @Override
            protected void _onError(String message) {
                mView.showErrorMsg(message, null);
            }

            @Override
            protected void _onCompleted() {
                mView.stopLoading();
            }
        });
    }

    @Override
    public void querySettlementInfo(String userId, String token, String company) {
        toSubscribe(HttpManager.getApi().querySettlementInfo(userId, token, company), new HttpSubscriber<SettlementInfoBean>() {
            @Override
            public void _onStart() {
                mView.showLoading("查询结算单中...");
            }

            @Override
            protected void _onNext(SettlementInfoBean bean) {
                mView.querySettlementSuccess(bean);
            }

            @Override
            protected void _onError(String message) {
                mView.showErrorMsg(message, null);
            }

            @Override
            protected void _onCompleted() {
                mView.stopLoading();
            }
        });
    }
}
