package com.honglu.future.ui.usercenter.contract;

import com.honglu.future.base.BaseView;
import com.honglu.future.ui.trade.bean.AccountBean;

/**
 * Created by zq on 2017/10/24.
 */

public interface UserCenterContract {
    interface View extends BaseView {
        void loginSuccess(AccountBean bean);
    }
    interface Presenter{
        void login(String account, String password,String userId,String company);
    }
}
