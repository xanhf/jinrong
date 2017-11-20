package com.honglu.future.http;

import com.google.gson.JsonNull;
import com.honglu.future.bean.BaseResponse;
import com.honglu.future.bean.UpdateBean;
import com.honglu.future.ui.details.bean.ConsultDetailsBean;
import com.honglu.future.ui.home.bean.BannerData;
import com.honglu.future.ui.home.bean.HomeIcon;
import com.honglu.future.ui.home.bean.HomeMarketCodeBean;
import com.honglu.future.ui.home.bean.HomeMessageItem;
import com.honglu.future.ui.home.bean.MarketData;
import com.honglu.future.ui.home.bean.NewsFlashData;
import com.honglu.future.ui.main.bean.AuditedBean;
import com.honglu.future.ui.market.bean.MarketnalysisBean;
import com.honglu.future.ui.recharge.bean.RechangeDetailData;
import com.honglu.future.ui.register.bean.RegisterBean;
import com.honglu.future.ui.trade.bean.AccountBean;
import com.honglu.future.ui.trade.bean.CloseBuiderBean;
import com.honglu.future.ui.trade.bean.ConfirmBean;
import com.honglu.future.ui.trade.bean.EntrustBean;
import com.honglu.future.ui.trade.bean.HistoryBuiderPositionBean;
import com.honglu.future.ui.trade.bean.HistoryClosePositionBean;
import com.honglu.future.ui.trade.bean.HistoryMissPositionBean;
import com.honglu.future.ui.trade.bean.HistoryTradeBean;
import com.honglu.future.ui.trade.bean.HoldDetailBean;
import com.honglu.future.ui.trade.bean.HoldPositionBean;
import com.honglu.future.ui.trade.bean.KLineBean;
import com.honglu.future.ui.trade.bean.ProductListBean;
import com.honglu.future.ui.trade.bean.RealTimeBean;
import com.honglu.future.ui.trade.bean.SettlementInfoBean;
import com.honglu.future.ui.trade.bean.TickChartBean;
import com.honglu.future.ui.usercenter.bean.AccountInfoBean;
import com.honglu.future.ui.usercenter.bean.BindCardBean;
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
    Observable<BaseResponse<UserInfoBean>> register(@Field("code") String code,
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

    //期货账户退出
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/user/login/out")
    Observable<BaseResponse> accountLogout(@Field("userId") String userId,
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

    //获取产品列表
    @POST("futures-mobile-api/app/future/exchange/trade/product/list")
    Observable<BaseResponse<List<ProductListBean>>> getProductList();

    //查询持仓列表
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/trade/holdPosition/list")
    Observable<BaseResponse<List<HoldPositionBean>>> getHoldPositionList(@Field("userId") String userId,
                                                                         @Field("token") String token,
                                                                         @Field("company") String company);

    //查询持仓列表明细
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/trade/holdPosition/detail")
    Observable<BaseResponse<List<HoldDetailBean>>> getHoldPositionDetail(@Field("instrumentId") String instrumentId,
                                                                         @Field("type") String type,
                                                                         @Field("todayPosition") String todayPosition,
                                                                         @Field("userId") String userId,
                                                                         @Field("token") String token);

    //查询委托中列表
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/trade/entrust/list")
    Observable<BaseResponse<List<EntrustBean>>> getEntrustList(@Field("userId") String userId,
                                                               @Field("token") String token);

    //委托撤单
    @FormUrlEncoded
    @POST("/futures-mobile-api/app/future/exchange/trade/cancel/order")
    Observable<BaseResponse> cancelOrder(@Field("orderRef") String orderRef,
                                         @Field("instrumentId") String instrumentId,
                                         @Field("sessionId") String sessionId,
                                         @Field("frontId") String frontId,
                                         @Field("userId") String userId,
                                         @Field("token") String token);

    //委托建仓
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/trade/create/order/v2")
    Observable<BaseResponse> buildTransaction(@Field("orderNumber") String orderNumber,
                                              @Field("type") String type,
                                              @Field("price") String price,
                                              @Field("instrumentId") String instrumentId,
                                              @Field("userId") String userId,
                                              @Field("token") String token,
                                              @Field("company") String company);

    //快速建仓
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/trade/create/order")
    Observable<BaseResponse> fastTransaction(@Field("orderNumber") String orderNumber,
                                              @Field("type") String type,
                                              @Field("price") String price,
                                              @Field("instrumentId") String instrumentId,
                                              @Field("userId") String userId,
                                              @Field("token") String token);

    //委托平仓
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/trade/close/order/v2")
    Observable<BaseResponse> closeOrder(@Field("todayPosition") String todayPosition,
                                        @Field("userId") String userId,
                                        @Field("token") String token,
                                        @Field("orderNumber") String orderNumber,
                                        @Field("type") String type,
                                        @Field("price") String price,
                                        @Field("instrumentId") String instrumentId,
                                        @Field("holdAvgPrice") String holdAvgPrice,
                                        @Field("company") String company
    );

    //快速平仓
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/trade/close/order")
    Observable<BaseResponse> ksCloseOrder(@Field("todayPosition") String todayPosition,
                                          @Field("userId") String userId,
                                          @Field("token") String token,
                                          @Field("orderNumber") String orderNumber,
                                          @Field("type") String type,
                                          @Field("price") String price,
                                          @Field("instrumentId") String instrumentId,
                                          @Field("holdAvgPrice") String holdAvgPrice,
                                          @Field("company") String company);


    //获取产品详情
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/trade/product/detail")
    Observable<BaseResponse<ProductListBean>> getProductDetail(@Field("instrumentId") String instrumentId);

    //行情
    //http://192.168.85.126:8083/futures-data-mobile/quotation/realTime/main?deviceType=2
    @POST("futures-data-mobile/quotation/realTime/main")
    Observable<BaseResponse<MarketnalysisBean>> getMarketData();

    //获取k线行情
    @GET("futures-data-mobile/quotation/kChart")
    Observable<BaseResponse<KLineBean>> getKLineData(@Query("excode") String excode,
                                                     @Query("code") String code,
                                                     @Query("type") String type);

    //获取分时图数据
    @GET("futures-data-mobile/quotation/tickChart")
    Observable<BaseResponse<TickChartBean>> getTickData(@Query("excode") String excode,
                                                        @Query("code") String code);

    //查询商品行情详情
    @GET("futures-data-mobile/quotation/realTime")
    Observable<BaseResponse<RealTimeBean>> getProductRealTime(@Query(value = "codes", encoded = true) String codes);

    //app版本更新查询接口
    @FormUrlEncoded
    @POST("futures-mobile-api/appVer/queryAppVersion")
    Observable<BaseResponse<UpdateBean>> getUpdateVersion(@Field("appType") String appType,
                                                          @Field("versionNumber") String versionNumber);


    /*******************************    上传图片   *****************************************/
    /*******************************
     * 上传图片
     *****************************************/
    @Multipart
    @POST("futures-mobile-api/user/info/update/avatar")
    Observable<BaseResponse> uploadUserAvatar(
            @Part MultipartBody.Part File, @Part("userId") RequestBody userId);

    //首页banner
    @POST("futures-mobile-api/appBanner/loadBannerInfo")
    Observable<BaseResponse<List<BannerData>>> getBannerData();

    //首页市场情codes
    @GET("futures-mobile-api/news/api/startup/init/v3")
    Observable<BaseResponse<HomeMarketCodeBean>> getMarketCodesData(@Query("sourceId") Integer account,
                                                                    @Query("versionNo") String version,
                                                                    @Query("userId") String userId);

    //首页市场行情List
    @GET("futures-data-mobile/quotation/realTime")
    Observable<BaseResponse<MarketData>> getMarketCodesData(
            @Query(value = "codes", encoded = true) String codes, @Query("deviceType") int deviceType
    );

    //首页icon
    @POST("futures-mobile-api/app/homeIcon/iconList")
    Observable<BaseResponse<List<HomeIcon>>> getHomeIcon();

    //首页新闻
    @FormUrlEncoded
    @POST("futures-mobile-api/app/information/informationList")
    Observable<BaseResponse<List<HomeMessageItem>>> getNewsColumnData(@Field("userId") String userId);

    //首页24小时
    @POST("futures-mobile-api/app/index/newsList")
    Observable<BaseResponse<List<NewsFlashData>>> geFlashNewData(@Query("pageIndex") int page, @Query("pageSize") int pageSize);

    //渠道是否过审  http://192.168.90.130:8080/
    @FormUrlEncoded
    @POST("futures-mobile-api/appChannel/isAudited")
    Observable<BaseResponse<AuditedBean>> getAudited(@Query("appType") int appType,
                                                     @Field("marketCode") String marketCode
            , @Field("versionNumber") String versionNumber);

    //银行卡列表
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/transfer/bank/list")
    Observable<BaseResponse<List<BindCardBean>>> geBindCardData(@Field("userId") String userId, @Field("token") String token);

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
                                                      @Field("flag") int flag,
                                                      @Field("userId") String userId);

    /**
     * https://www.showdoc.cc/1673161?page_id=15533135
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/information/informationDetail")
    Observable<BaseResponse<ConsultDetailsBean>> getMessageData(@Field("informationId") String informationId);

    /**
     * https://www.showdoc.cc/1673161?page_id=15533135
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/information/informationPraise")
    Observable<BaseResponse<List<String>>> praiseMessage(@Field("informationId") String informationId,
                                                         @Field("userId") String userID);


    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 历史订单订单统计
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/orders/history/stat")
    Observable<BaseResponse<HistoryTradeBean>> getHistoryTradeBean(
            @Field("dayStart") String dayStart,
            @Field("userId") String userId,
            @Field("token") String token,
            @Field("dayEnd") String dayEnd);

    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 历史已撤单列表
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/orders/history/cancel/list")
    Observable<BaseResponse<List<HistoryMissPositionBean>>> getHistoryMissBean(
            @Field("dayStart") String dayStart,
            @Field("userId") String userId,
            @Field("token") String token,
            @Field("dayEnd") String dayEnd,
            @Field("page") int page,
            @Field("pageSize") int pageSize);

    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 历史订单平仓列表
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/orders/history/close/list")
    Observable<BaseResponse<List<HistoryClosePositionBean>>> getHistoryCloseBean(
            @Field("dayStart") String dayStart,
            @Field("userId") String userId,
            @Field("token") String token,
            @Field("dayEnd") String dayEnd,
            @Field("page") int page,
            @Field("pageSize") int pageSize);

    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 历史订单平仓列表
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/orders/history/open/list")
    Observable<BaseResponse<List<HistoryBuiderPositionBean>>> getHistoryBuilderBean(
            @Field("dayStart") String dayStart,
            @Field("userId") String userId,
            @Field("token") String token,
            @Field("dayEnd") String dayEnd,
            @Field("page") int page,
            @Field("pageSize") int pageSize);

    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 平仓详情
     *
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/orders/close/details")
    Observable<BaseResponse<List<CloseBuiderBean>>> getCloseBuiderBean(
            @Field("userId") String userId,
            @Field("id") String id,
            @Field("token") String token);

    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 入金(充值)接口
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/transfer/recharge")
    Observable<BaseResponse<JsonNull>> getRechargeAsses(
            @Field("userId") String userId,
            @Field("brokerBranchId") String brokerBranchId,
            @Field("password") String password,
            @Field("bankId") String bankId,
            @Field("bankBranchId") String bankBranchId,
            @Field("bankAccount") String bankAccount,
            @Field("bankPassword") String bankPassword,
            @Field("amount") String amount,
            @Field("token") String token);
    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 出金(提现)接口
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/transfer/cashout")
    Observable<BaseResponse<JsonNull>> getCashoutAsses(
            @Field("userId") String userId,
            @Field("brokerBranchId") String brokerBranchId,
            @Field("password") String password,
            @Field("bankId") String bankId,
            @Field("bankBranchId") String bankBranchId,
            @Field("bankAccount") String bankAccount,
            @Field("bankPassword") String bankPassword,
            @Field("amount") String amount,
            @Field("token") String token);

    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 出入金明细(充值明细)接口
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/transfer/detail")
    Observable<BaseResponse<List<RechangeDetailData>>> getDetail(
            @Field("userId") String userId,
            @Field("page") int page,
            @Field("pageSize") int pageSize,
            @Field("token") String token);
    /**
     * https://www.showdoc.cc/1673161?page_id=15438333
     * 银行卡余额查询接口
     * @return
     */
    @FormUrlEncoded
    @POST("futures-mobile-api/app/future/exchange/transfer/select/balance")
    Observable<BaseResponse<String>> getBalanceAsses(
            @Field("userId") String userId,
            @Field("brokerBranchId") String brokerBranchId,
            @Field("password") String password,
            @Field("bankId") String bankId,
            @Field("bankBranchId") String bankBranchId,
            @Field("bankAccount") String bankAccount,
            @Field("bankPassword") String bankPassword,
            @Field("token") String token);

}
