package com.honglu.future.ui.login.activity;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.base.BaseActivity;
import com.honglu.future.events.LoginEvent;
import com.honglu.future.ui.login.contract.LoginContract;
import com.honglu.future.ui.login.presenter.LoginPresenter;
import com.honglu.future.ui.register.activity.RegisterActivity;
import com.honglu.future.ui.usercenter.bean.UserInfoBean;
import com.honglu.future.util.Tool;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.OnClick;

import static com.honglu.future.util.ToastUtil.showToast;

/**
 * Created by zq on 2017/10/24.
 */

public class LoginActivity extends BaseActivity<LoginPresenter> implements LoginContract.View {
    @BindView(R.id.tv_mobile)
    EditText mMobile;
    @BindView(R.id.tv_pwd)
    EditText mPwd;

    @Override
    public void showLoading(String content) {
        if (!TextUtils.isEmpty(content)) {
            App.loadingContent(this, content);
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
        return R.layout.activity_login;
    }

    @Override
    public void initPresenter() {
        mPresenter.init(this);
    }

    @Override
    public void loadData() {

    }

    @OnClick({R.id.tv_login, R.id.tv_forget_pwd, R.id.btn_login, R.id.iv_close})
    public void onClick(View view) {
        if (Tool.isFastDoubleClick()) return;
        switch (view.getId()) {
            case R.id.tv_login:
                Intent intent = new Intent(this, RegisterActivity.class);
                startActivity(intent);
                finish();
                break;
            case R.id.tv_forget_pwd:
                Intent resetPwd = new Intent(this, ResetPwdActivity.class);
                startActivity(resetPwd);
                finish();
                break;
            case R.id.btn_login:
                if (mMobile.length() < 11) {
                    showToast("请输入正确的手机号码");
                    return;
                }

                if (mPwd.length() < 6) {
                    showToast("密码必须为6~16字符");
                    return;
                }

                mPresenter.login(mMobile.getText().toString(),
                        mPwd.getText().toString());
                break;
            case R.id.iv_close:
                finish();
                break;
        }
    }

    @Override
    public void loginSuccess(UserInfoBean bean) {
        EventBus.getDefault().post(new LoginEvent(getApplicationContext(), bean));
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        //得到InputMethodManager的实例
        imm.hideSoftInputFromWindow(mPwd.getWindowToken(), 0);
        finish();
    }
}
