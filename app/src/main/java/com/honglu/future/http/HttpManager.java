package com.honglu.future.http;


import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.config.Constant;
import com.honglu.future.config.LogInterceptor;
import com.honglu.future.util.AndroidUtil;
import com.honglu.future.util.AppUtils;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.ViewUtil;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * Retrofit请求管理类<p>
 */
public class HttpManager {

    private HttpApi mHttpApi;

    private static HttpManager instance = null;

    private static OkHttpClient glideOkHttpClient = null;

    /**
     * 获取单例
     * @return 实例
     */
    public static HttpManager getInstance() {

        if (instance == null) {
            instance = new HttpManager();
            return instance;
        }
        return instance;
    }

    public static OkHttpClient getGlideOkHttpClient(){
        if(glideOkHttpClient == null){
            Cache cache = new Cache(new File(App.getContext().getCacheDir(), "xnsudaiCache"),
                    1024 * 1024 * 100);
            //CookieManager管理器
            CookieManager cookieManager = new CookieManager();
            cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

            HttpsUtils.SSLParams sslParams = HttpsUtils.getSslSocketFactory(null, null, null);
            glideOkHttpClient = new OkHttpClient.Builder()
                    .cache(cache)   //缓存
                    .sslSocketFactory(sslParams.sSLSocketFactory)
                    .cookieJar(new JavaNetCookieJar(cookieManager))//设置持续化cookie
                    .retryOnConnectionFailure(true)//失败重连
                    .connectTimeout(20, TimeUnit.SECONDS)
                    .readTimeout(20, TimeUnit.SECONDS)
                    .writeTimeout(20, TimeUnit.SECONDS)
                    .hostnameVerifier(new HostnameVerifier() {
                        @Override
                        public boolean verify(String hostname, SSLSession session) {
                           return true;
                        }
                    })
                    .build();
        }
        return glideOkHttpClient;
    }

    public static HttpApi getApi() {
        return getInstance().mHttpApi;
    }

    private HttpManager() {
        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd HH:mm:ss")
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(App.getConfig().getBaseUrl())
                .client(createOkHttpClient())
                //.addConverterFactory(ScalarsConverterFactory.create()) 返回类型转成String
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        mHttpApi = retrofit.create(HttpApi.class);
    }

    public OkHttpClient createOkHttpClient() {

        Cache cache = new Cache(new File(App.getContext().getCacheDir(), "xnsudaiCache"),
                1024 * 1024 * 100);

        //添加全局统一请求头
        Interceptor headerInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request.Builder builder = chain.request().newBuilder();
                String sessionid = SpUtil.getString(Constant.CACHE_TAG_SESSIONID);
                if (App.getConfig().getLoginStatus() && !TextUtils.isEmpty(sessionid)) {
                    builder.addHeader("Cookie", "SESSIONID=" + sessionid);
                }
                Response response = chain.proceed(builder.build());
                return response;
            }
        };
        //添加全局统一请求参数
        Interceptor paramsInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                HttpUrl httpUrl = request.url();
                HttpUrl url = httpUrl.newBuilder()
                        .addQueryParameter("clientType", "android")
                        .addQueryParameter("appVersion", ViewUtil.getAppVersion(App.getContext()))
                        .addQueryParameter("deviceId", ViewUtil.getDeviceId(App.getContext()))
                        .addQueryParameter("mobilePhone", App.getConfig().getLoginStatus() ? SpUtil.getString
                                (Constant.CACHE_TAG_USERNAME) : "")
                        .addQueryParameter("deviceName", ViewUtil.getDeviceName().trim())
                        .addQueryParameter("osVersion", ViewUtil.getOsVersion())
                        .addQueryParameter("appName", App.getContext().getResources().getString(R.string.app_short))
                        .addQueryParameter("appMarket", App.getConfig().getChannelName()).build();
                Request.Builder builder = request.newBuilder().url(url);
                builder.addHeader("User-Agent", AndroidUtil.getUA(App.getContext()));
                Response response = chain.proceed(builder.build());
                return response;
            }
        };
        //是否显示"请求繁忙"倒计时dialog  PS：需要显示的在HttpApi接口添加请求头@Headers("showDialog:true")
        Interceptor showDialogInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request request = chain.request();
                String showDialog = request.header("showDialog");
                Response response = chain.proceed(chain.request());
                String body = response.body().string();
                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(body);
                    //String code = jsonResponse.getString("errorCode");
                    //if ("-3".equals(code)) {//需与服务器协商,返回-3
                        if (TextUtils.isEmpty(showDialog) || !showDialog.equals("true")) {
                            jsonResponse.put("code", "-1");
                        }
                   // }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                return response.newBuilder().body(ResponseBody.create(MediaType.parse("UTF-8"), jsonResponse.toString
                        ())).build();
            }
        };

        //日志拦截器
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Logger.t("http").e(message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        //CookieManager管理器
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        InputStream inputStream = App.mApp.getResources().openRawResource(R.raw.calffutuer);
        OkHttpClient mOkHttpClient = new OkHttpClient.Builder()
                .cache(cache)   //缓存
                .sslSocketFactory(HttpsUtils.getCertificates(inputStream))
                .addInterceptor(headerInterceptor)
                .addInterceptor(paramsInterceptor)
                .addInterceptor(new LogInterceptor())
                .addInterceptor(showDialogInterceptor)
                .cookieJar(new JavaNetCookieJar(cookieManager))//设置持续化cookie
                .addInterceptor(logging)    //打印日志
                .retryOnConnectionFailure(true)//失败重连
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .hostnameVerifier(HttpsUtils.getHostnameVerifier())
                .build();
        return mOkHttpClient;
    }

    /**
     * 给url添加全局统一请求参数信息
     * @param url
     * @return
     */
    public static String getUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        String ret_url = "";
        if (url.contains("clientType=android&appVersion=")) {

            return url;
        } else {
            if (url.contains("?")) {
                ret_url = url + "&";
            } else {
                ret_url = url + "?";
            }
            ret_url += "version=" + AppUtils.getVersionName() + "&mobile=" + SpUtil.getString(Constant.CACHE_TAG_MOBILE) + "&userId=" + SpUtil.getString(Constant.CACHE_TAG_UID)+"&clientType=android&appVersion="
                    + ViewUtil.getAppVersion(App.getContext()) + "&deviceId="
                    + ViewUtil.getDeviceId(App.getContext()) + "&mobilePhone=" +
                    (App.getConfig().getLoginStatus() ? SpUtil.getString(Constant.CACHE_TAG_USERNAME) : "")
                    + "&deviceName=" + ViewUtil.getDeviceName() + "&osVersion="
                    + ViewUtil.getOsVersion() + "&appMarket="
                    + App.getConfig().getChannelName() + "&appName="
                    +App.getContext().getResources().getString(R.string.app_short);
            return ret_url.replace(" ", "");
        }
    }

    /**
     * 处理线程调度
     * @param <T>
     * @return
     */
    public <T> Observable.Transformer<T, T> applySchedulers() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(Observable<T> tObservable) {
                return tObservable
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .unsubscribeOn(Schedulers.io());
            }
        };
    }

//    public SSLSocketFactory getCertificateFactory(Context context)
//    {
//        try {
//            CertificateFactory cf = CertificateFactory.getInstance("X.509");
//            InputStream cert = context.getAssets().open("nginx.cer");
//            Certificate ca = cf.generateCertificate(cert);
//            cert.close();
//
//            // creating a KeyStore containing our trusted CAs
//            String keyStoreType = KeyStore.getDefaultType();
//            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
//            keyStore.load(null, null);
//            keyStore.setCertificateEntry("ca", ca);
//
//            return new AdditionalKeyStore(keyStore);
//        } catch(Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }


    public final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
}
