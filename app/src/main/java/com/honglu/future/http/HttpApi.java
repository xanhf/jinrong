package com.honglu.future.http;

import com.google.gson.JsonNull;
import com.honglu.future.bean.BaseResponse;
import com.honglu.future.ui.home.bean.BannerData;
import com.honglu.future.ui.home.bean.HomeIcon;
import com.honglu.future.ui.home.bean.HomeMessageItem;
import com.honglu.future.ui.home.bean.MarketData;
import com.honglu.future.ui.home.bean.NewsFlashData;
import com.honglu.future.ui.register.bean.RegisterBean;
import com.honglu.future.ui.trade.bean.AccountBean;
import com.honglu.future.ui.trade.bean.ConfirmBean;
import com.honglu.future.ui.trade.bean.ProductListBean;
import com.honglu.future.ui.trade.bean.SettlementInfoBean;
import com.honglu.future.ui.usercenter.bean.AccountInfoBean;
import com.honglu.future.ui.usercenter.bean.UserInfoBean;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;
import rx.Observable;


/**
 *
 */
public interface HttpApi {

    //所有需要的泛型 添加：BaseResponse<UserInfo>
    //用户注册
    @FormUrlEncoded
    @POST("futures-mobile-api/user/info/register")
    Observable<BaseResponse<RegisterBean>> register(@Field("code") String code,
                                                    @Field("sourceId") String sourceId,
                                                    @Field("mobileNum") String mobileNum,
                                                    @Field("password") String password,
                                                    @Field("nickName") String nickName);

    //用户注册发送验证码
    @FormUrlEncoded
    @POST("futures-mobile-api/user/info/register/send/sms")
    Observable<BaseResponse> getCode(@Field("sourceId") String sourceId,
                                     @Field("mobileNum") String mobileNum);

    //找回密码(重置密码)发送验证码
    @FormUrlEncoded
    @POST("futures-mobile-api/user/info/retrieve/password/send/sms")
    Observable<BaseResponse> getResetCode(@Field("sourceId") String sourceId,
                                          @Field("mobileNum") String mobileNum);

    //找回密码
    @FormUrlEncoded
    @POST("futures-mobile-api/user/info/retrieve/password")
    Observable<BaseResponse> resetPwd(@Field("mobileNum") String mobileNum,
                                      @Field("password") String password,
                                      @Field("code") String code);

    //用户登录
    @FormUrlEncoded
    @POST("futures-mobile-api/user/info/login")
    Observable<BaseResponse<UserInfoBean>> login(@Field("mobileNum") String mobileNum,
                                                 @Field("password") String password);

    //修改昵称
    @FormUrlEncoded
    @POST("futures-mobile-api/user/info/update/nickName")
    Observable<BaseResponse> updateNickName(@Field("nickName") String nickName,
                                            @Field("userId") String userId);

    //交易所用户登录
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/user/login")
    Observable<BaseResponse<AccountBean>> loginAccount(@Field("account") String account,
                                                       @Field("password") String password,
                                                       @Field("userId") String userId,
                                                       @Field("company") String company);

    //获取用户账户基本信息
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/user/account/info")
    Observable<BaseResponse<AccountInfoBean>> getAccountInfo(@Field("userId") String userId,
                                                             @Field("token") String token,
                                                             @Field("company") String company);

    //获取用户结算单
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/user/settlement/info")
    Observable<BaseResponse<SettlementInfoBean>> querySettlementInfo(@Field("userId") String userId,
                                                                     @Field("token") String token,
                                                                     @Field("company") String company);

    //根据日期获取用户结算单
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/user/settlement/info/select")
    Observable<BaseResponse<SettlementInfoBean>> querySettlementInfoByDate(@Field("userId") String userId,
                                                                           @Field("token") String token,
                                                                           @Field("company") String company,
                                                                           @Field("day") String day);

    //结算单确认
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/user/settlement/info/confirm")
    Observable<BaseResponse<ConfirmBean>> settlementConfirm(@Field("userId") String userId,
                                                            @Field("token") String token);

    //结算单确认
    @POST("futures-mobile-api/app/future/exchange/trade/product/list")
    Observable<BaseResponse<List<ProductListBean>>> getProductList();


    /*******************************    上传图片   *****************************************/
    /*******************************
     * 上传图片
     *****************************************/
    @Multipart
    @POST("futures-mobile-api/user/info/update/avatar")
    Observable<BaseResponse> uploadUserAvatar(
            @Part MultipartBody.Part File, @Part("userId") RequestBody userId);

    //首页banner
    @POST("http://192.168.90.130:8080/futures-mobile-api/appBanner/loadBannerInfo")
    Observable<BaseResponse<List<BannerData>>> getBannerData();

    //首页市场行情
    @GET("credit-user/register")
    Observable<BaseResponse<MarketData>> getMarketData();

    //首页icon
    @GET("credit-user/register")
    Observable<BaseResponse<HomeIcon>> getHomeIcon();

    //首页新闻
    @GET("credit-user/register")
    Observable<BaseResponse<HomeMessageItem>> getNewsColumnData();

    //首页24小时
    @POST("http://192.168.90.130:8080/futures-mobile-api/app/index/newsList")
    Observable<BaseResponse<List<NewsFlashData>>> geFlashNewData(@Query("pageIndex") int page,@Query("pageSize") int pageSize);

    //修改资金密码接口 测试环境：

    /**
     * 开发环境： http://192.168.90.162:8080/mobileApi/app/future/exchange/user/update/trade/password
     * 测试环境： http://192.168.85.126:8081/futures-mobile-api/app/future/exchange/user/update/trade/password
     * 待调试
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/user/update/trade/password")
    Observable<BaseResponse<JsonNull>> resetAssesPwd(@Field("account") String account,
                                                     @Field("oldPassword") String oldPassword,
                                                     @Field("token") String token,
                                                     @Field("newPassword") String newPassword,
                                                     @Field("userId") String userId);

    /**
     * 开发环境： http://192.168.90.162:8080/mobileApi/app/future/exchange/user/update/password
     * 测试环境： http://192.168.85.126:8081/futures-mobile-api/app/future/exchange/user/update/password
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/user/update/password")
    Observable<BaseResponse<JsonNull>> resetMarketPwd(@Field("account") String account,
                                                      @Field("oldPassword") String oldPassword,
                                                      @Field("token") String token,
                                                      @Field("newPassword") String newPassword,
                                                      @Field("userId") String userId);

}
