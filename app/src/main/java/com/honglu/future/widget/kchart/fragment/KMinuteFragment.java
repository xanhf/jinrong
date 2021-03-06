package com.honglu.future.widget.kchart.fragment;

import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.honglu.future.R;
import com.honglu.future.app.App;
import com.honglu.future.config.Constant;
import com.honglu.future.dialog.KlineGuideDialog;
import com.honglu.future.ui.trade.bean.KLineBean;
import com.honglu.future.ui.trade.bean.TickChartBean;
import com.honglu.future.ui.trade.fragment.PagerFragment;
import com.honglu.future.util.SpUtil;
import com.honglu.future.util.TimeUtil;
import com.honglu.future.util.ToastUtil;
import com.honglu.future.widget.kchart.chart.cross.KCrossLineView;
import com.honglu.future.widget.kchart.chart.minute.KMinuteView;
import com.xulu.mpush.message.KCandleObj;
import com.honglu.future.widget.kchart.listener.OnKChartClickListener;
import com.honglu.future.widget.kchart.listener.OnKCrossLineMoveListener;
import com.honglu.future.widget.kchart.listener.OnKLineTouchDisableListener;
import com.xulu.mpush.message.RequestMarketMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

import static com.honglu.future.util.ToastUtil.showToast;

/**
 * Created by fangzhu v4.0.
 * <p/>
 * 改版后的分时图 fragment
 */
public class KMinuteFragment extends PagerFragment implements KLineContract.View, OnKLineTouchDisableListener, OnKChartClickListener {
    public static final String TAG = "KMinuteFragment";
    @BindView(R.id.minuteView)
    KMinuteView minuteView;
    @BindView(R.id.crossLineView)
    KCrossLineView crossLineView;
    private String mCloseTimeStr = "";
    // View layoutLoding;

    double closed;
    /*
        处理baksource 数据不完整的情况
        如 深圳成指  非WIFI下 获取到的数据不完整 服务端的接口问题
        在此做特殊处理
    */
    int TIME_STOP_MIN = 125;
    //产品code
    String excode, code;
    private KLinePresenter mKLinePresenter;
    private boolean mIsOneGuide; //是否开启双击全屏引导

    @Override
    public int getLayoutId() {
        return R.layout.frag_kminute;
    }

    @Override
    public void initPresenter() {
        mKLinePresenter = new KLinePresenter();
        mKLinePresenter.init(this);
    }

    @Override
    public void showLoading(String content) {
        if (!TextUtils.isEmpty(content)) {
            App.loadingDefault(mActivity);
        }
    }

    @Override
    protected void onVisible() {
        super.onVisible();
        mKLinePresenter.getTickData(excode, code);
    }

    @Override
    protected void onInvisible() {
        super.onInvisible();
    }

    public void setExcode(String excode) {
        this.excode = excode;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setClosed(String closed) {
        this.closed = Double.parseDouble(closed);
    }

    public void setTimeStr(String timeStr) {
        mCloseTimeStr = timeStr;
    }

//    @Override
//    public void onHiddenChanged(boolean hidden) {
//        super.onHiddenChanged(hidden);
//        if (!hidden) {
//                if (isVisible()) {
//                    mPresenter.getKLineData("SHFE", "AG1801", "10");
//                }
//        }
//    }

    @Override
    public void stopLoading() {
        App.hideLoading();
    }

    @Override
    public void showErrorMsg(String msg, String type) {
        showToast(msg);
    }

    @Override
    public void loadData() {
        //closed = Double.parseDouble(ConvertUtil.NVL(getArguments().getString("closed"), "0"));
//        code = getArguments().getString("code");
//        type = getArguments().getString("type");
        mIsOneGuide = SpUtil.getBoolean(Constant.GUIDE_KLINE_FULLSCREEN, false);
        if (!TextUtils.isEmpty(code)) {
            // int numberScale = ProFormatConfig.getProFormatMap(type + "|" + code.split("(\\d+)")[0]);
            //minuteView.setNumberScal(numberScale != -1 ? numberScale : 2);
        }

        excode = getArguments().getString("exCode");
        code = getArguments().getString("code");
        closed = getArguments().getDouble("closePrice");

        minuteView.setCrossLineView(crossLineView);
        //阻断touch事件的分发逻辑  listView headerView,这里还是用listview的onitemClick，touch太容易触发
//        minuteView.setOnKLineTouchDisableListener(this);
        minuteView.setOnKChartClickListener(this);
        minuteView.setAsixTitlein(true);
        // 0829添加成交量 add by haiyang
        minuteView.setShowSubChart(true);//需要画成交量的指标
        minuteView.setZuoClosed(closed);
//        minuteView.setStartTimeStr(getArguments().getString("startTimeStr"));
//        minuteView.setStopTimeStr(getArguments().getString("stopTimeStr"));
        minuteView.setMiddleTimeStr(mCloseTimeStr);

        minuteView.setAsixXByTime(false);


//        if (Configuration.ORIENTATION_LANDSCAPE == getResources().getConfiguration().orientation) {
//            minuteView.setTouchEnable(true);
//        } else {
//            minuteView.setTouchEnable(false);
//
//        }

        //点击出现十字线监听
        minuteView.setOnKCrossLineMoveListener(new OnKCrossLineMoveListener() {
            @Override
            public void onCrossLineMove(KCandleObj object) {
                if (object != null) {
                    initCrossView(object);
                }
            }

            @Override
            public void onCrossLineHide() {

            }
        });


        //layoutLoding = view.findViewById(R.id.layoutLoding);
    }

    /**
     * 刷新最后一个K线
     *
     * @param optional
     */
    public void setLastKData(RequestMarketMessage optional) {
        KCandleObj toAddendK = optional.obj2KCandleObj();
        if (toAddendK != null) {
            List<KCandleObj> list = minuteView.getkCandleObjList();
            if (list == null || list.size() == 0)
                return;
            KCandleObj lastK = list.get(list.size() - 1);
            //都转换成分钟
            String lastKT = TimeUtil.formatDate(new Date(lastK.getTimeLong()), "yyyy-MM-dd HH:mm");
            String toAddendKT = TimeUtil.formatDate(new Date(toAddendK.getTimeLong()), "yyyy-MM-dd HH:mm");

            //需要更新的和最后一个是同一个
            if (toAddendK.getTimeLong() >= lastK.getTimeLong()
                    && lastKT.equals(toAddendKT)) {
                Log.v(TAG, "remove");
                list.remove(lastK);
            }
            Log.v(TAG, "toAddendKT=" + toAddendKT);
            Log.v(TAG, "lastK=" + lastK);
            toAddendK.setTime(TimeUtil.formatDate(new Date(toAddendK.getTimeLong()), "yyyy-MM-dd HH:mm"));
            if (toAddendK.getTimeLong() >= lastK.getTimeLong()) {
                Log.v(TAG, "add");
                toAddendK.setClose(toAddendK.getClose());
                list.add(toAddendK);
            }
            minuteView.setkCandleObjList(list);
        }
    }

    @Override
    public void event() {

    }

    //touch 出现十字线的逻辑
    void initCrossView(KCandleObj entity) {

    }

    List<KCandleObj> kCandleObjs;

    @Override
    public boolean onSingleClick() {
        return false;
    }

    @Override
    public boolean onDoubleClick() {
        //双击横屏竖屏切换
        if (Configuration.ORIENTATION_LANDSCAPE == getResources()
                .getConfiguration().orientation) {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        } else {
            getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        return false;
    }

    @Override
    public boolean onLongPress() {
        return false;
    }

    @Override
    public void getKLineDataSuccess(KLineBean bean) {

    }

    @Override
    public void getTickDataSuccess(TickChartBean bean) {
        if (bean == null || bean.getList() == null || bean.getList().size() == 0) {
            showToast("暂无数据");
            return;
        }
        List<KCandleObj> list = new ArrayList<KCandleObj>();
        List<TickChartBean.Data> kLineList = bean.getList();

        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd HH:mm");
        //开盘休盘停盘时间段
        String startTime = bean.getStartTime();
        String endTime = bean.getEndTime();
        String middleTime = bean.getCloseTime();
        double totalVol = Double.valueOf(bean.getTotalVolume());
        long reqTime = bean.getReqTime();
        double sum = 0;
        for (int i = 0; i < kLineList.size(); i++) {
            KCandleObj entity = new KCandleObj();
            TickChartBean.Data tickbean = kLineList.get(i);

            //分时的开盘收盘这里设置成一样
            entity.setClose(Double.valueOf(tickbean.getP()));
            entity.setOpen(Double.valueOf(tickbean.getP()));
            entity.setTimeLong(Long.parseLong(tickbean.getT()));
            entity.setTime(sdf.format(entity.getTimeLong()));
            // 0829添加成交量 add by haiyang
            entity.setVol(Double.valueOf(tickbean.getV()));

            entity.setStartTime(startTime);
            entity.setEndTime(endTime);
            entity.setMiddleTime(middleTime);
            entity.setTotalVol(totalVol);
            entity.setReqTime(reqTime);

            //绘制均线使用
            sum += entity.getClose();
            entity.setNormValue(sum / (i + 1));

            list.add(0, entity);

        }
        //时间降序排列
        Collections.sort(list, new Comparator<KCandleObj>() {
            @Override
            public int compare(KCandleObj obj, KCandleObj t1) {
                return (obj.getTimeLong() - t1.getTimeLong() > 0) ? 1 : -1;
            }
        });
        doPostExecute(list, true);
    }

//    class GetKlineDataTask extends AsyncTask<String, Void, List<KCandleObj>> {
//
//        @Override
//        protected void onPreExecute() {
//            // TODO Auto-generated method stub
//            super.onPreExecute();
////            minuteView.setVisibility(View.INVISIBLE);
////            if (layoutLoding != null)
////                layoutLoding.setVisibility(View.VISIBLE);
//        }

//        @Override
//        protected List<KCandleObj> doInBackground(String... params) {
//            // TODO Auto-generated method stub
//            try {
//                String source = getArguments().getString("source");
//                if (BaseInterface.isBakSource) {
//                    String code = getArguments().getString("code");
//                    String id = getArguments().getInt("goodsid") + "";
//
//                    if (BakSourceInterface.specialSource.contains(source)) {
//                        kCandleObjs = KLineHelper.getKlineTime4Weipan(source, code);
//                    } else {
//                        kCandleObjs = KLineHelper.getKlineTime(source, code, BakSourceInterface.PARAM_KLINE_TIME);
//
//                        //是否需要做接口数据不完整问题处理  全球沪指start
//                        int stopPos = -1;
//                        for (int i = 0; i < kCandleObjs.size(); i++) {
//                            KCandleObj kLineEntity = kCandleObjs.get(i);
//                            KCandleObj kLineEntityAfter = null;
//                            if (i + 1 < kCandleObjs.size())
//                                kLineEntityAfter = kCandleObjs.get(i + 1);
//
//                            if (kLineEntityAfter != null && kLineEntityAfter.getTimeLong() - kLineEntity.getTimeLong() > TIME_STOP_MIN * 60 * 1000) {
//                                Log.v(TAG, "get promble pos =" + i + " getTimelong=" + kLineEntity.getTimeLong() + " kLineEntityAfter.getTimelong()=" + kLineEntityAfter.getTimeLong());
//                                stopPos = i;
//                                break;
//                            }
//                        }
//                        if (stopPos != -1)
//                            kCandleObjs = kCandleObjs.subList(0, stopPos);
//                        //是否需要做接口数据不完整问题处理  end
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//                return null;
//            }
//            return kCandleObjs;
//        }

//        @Override
//        protected void onPostExecute(List<KCandleObj> result) {
//            // TODO Auto-generated method stub
//            super.onPostExecute(result);
//            if (!isAdded())
//                return;
//
//            doPostExecute(result, true);
//        }
//
//    }

    public void doPostExecute(List<KCandleObj> result, boolean isToast) {
        minuteView.setVisibility(View.VISIBLE);
//        if (layoutLoding != null)
//            layoutLoding.setVisibility(View.GONE);

        if (result == null) {
            if (isToast) {
                ToastUtil.show("数据获取失败");
            }
            return;
        }

        if (result == null || result.size() == 0) {
            if (isToast) {
                ToastUtil.show("暂无数据");
            }
            return;
        }
        if (result.size() > 0 && result.size() < 2) {
            //只有一条数据没法画出分时线的区域面积  这里当作没有数据
            result.add(result.get(result.size() - 1));
        }
        minuteView.setkCandleObjList(result);
        minuteView.postInvalidate();
        if (!mIsOneGuide) {
            mIsOneGuide = true;
            new KlineGuideDialog(mContext).show();
        }
//            minuteView.perShowCross();//默认显示十字线

//            //需要layout布局完成之后
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    minuteView.perShowCross();//默认显示十字线
//                }
//            }, 200);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mPresenter != null) {
            mPresenter.onDestroy();
        }
    }


//    /**
//     * 刷新最后一个K线
//     *
//     * @param optional 长连接过来的是总的成交量
//     *                 所以当前成交量==长连接总vol(x)-请求时间的总成交量(y)+最后一根k线的成交量
//     */
//    public void setLastKData(Optional optional) {
//
//        KCandleObj toAddendK = optional.obj2KCandleObj();
//        if (toAddendK != null) {
//            List<KCandleObj> list = minuteView.getkCandleObjList();
//            if (list == null || list.size() == 0)
//                return;
//            KCandleObj lastK = list.get(list.size() - 1);
//            //都转换成分钟
//            String lastKT = DateUtil.formatDate(new Date(lastK.getTimeLong()), "yyyy-MM-dd HH:mm");
//            String toAddendKT = DateUtil.formatDate(new Date(toAddendK.getTimeLong()), "yyyy-MM-dd HH:mm");
//            String reqTime = DateUtil.formatDate(new Date(lastK.getReqTime()), "yyyy-MM-dd HH:mm");
//            //需要更新的和最后一个是同一个(在同一分钟以内)
//            if (toAddendK.getTimeLong() >= lastK.getTimeLong() && lastKT.equals(toAddendKT)) {
//                Log.v(TAG, "remove");
//                list.remove(lastK);
//            }
//            Log.v(TAG, "toAddendKT=" + toAddendKT);
//            Log.v(TAG, "lastK=" + lastKT);
//            toAddendK.setTime(DateUtil.formatDate(new Date(toAddendK.getTimeLong()), "yyyy-MM-dd HH:mm"));
//            if (toAddendK.getTimeLong() >= lastK.getTimeLong()) {
//                Log.v(TAG, "add");
//                toAddendK.setClose(toAddendK.getClose());
//
//                // 更新成交量
//                if (lastKT.equals(toAddendKT)) {// 在同一分钟之内
////                    Log.e(TAG,"同一分钟===="+(toAddendK.getTotalVol() - lastK.getTotalVol() + lastK.getVol()));
//                    toAddendK.setVol(toAddendK.getTotalVol() - lastK.getTotalVol() + lastK.getVol());
//                } else {// 不是同一分钟
////                    Log.e(TAG,"不是同一分钟===="+(toAddendK.getTotalVol() - lastK.getTotalVol() ));
//                    toAddendK.setVol(toAddendK.getTotalVol() - lastK.getTotalVol());
//                    toAddendK.setReqTime(toAddendK.getTimeLong());
//                }
//                list.add(toAddendK);
//            }
//            minuteView.setkCandleObjList(list);
//        }
//    }

//    /**
//     * 绘制持仓单辅助线
//     *
//     * @param listOrder
//     */
//    public void setHelpLineForTradeOrder(List<TradeOrder> listOrder) {
//        minuteView.setHelpLineForTradeOrder(listOrder);
//    }
//
//    /**
//     * 绘制行情提醒辅助线
//     *
//     * @param listProductNotice
//     */
//    public void setHelpLineForProductNotice(List<ProductNotice> listProductNotice) {
//        minuteView.setHelpLineForProductNotice(listProductNotice);
//    }
}
