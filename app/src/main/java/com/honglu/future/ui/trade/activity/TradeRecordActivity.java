package com.honglu.future.ui.trade.activity;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.honglu.future.R;
import com.honglu.future.base.BaseActivity;
import com.honglu.future.config.Constant;
import com.honglu.future.dialog.DateDialog;
import com.honglu.future.ui.trade.adapter.TradeRecordAdapter;
import com.honglu.future.ui.trade.bean.HistoryBuiderPositionBean;
import com.honglu.future.ui.trade.bean.HistoryClosePositionBean;
import com.honglu.future.ui.trade.bean.HistoryMissPositionBean;
import com.honglu.future.ui.trade.bean.HistoryTradeBean;
import com.honglu.future.ui.trade.contract.TradeRecordContract;
import com.honglu.future.ui.trade.presenter.TradeRecordPresenter;
import com.honglu.future.util.SpUtil;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import butterknife.BindView;

/**
 * 交易记录
 * Created by zhuaibing on 2017/10/26
 */

public class TradeRecordActivity extends BaseActivity<TradeRecordPresenter> implements View.OnClickListener,TradeRecordContract.View{


    @BindView(R.id.lv_listView)
    ListView lvListView;
    @BindView(R.id.refreshView)
    SmartRefreshLayout refreshView;
    @BindView(R.id.tv_back)
    ImageView mIvBack;
    TextView tvOne;
    TextView tvTwo;
    TextView tvThr;
    TextView tvFour;

    private TextView tvStartTime;
    private TextView tvEndTime;
    private TextView tvRisk;
    private TextView tvJiancang;
    private TextView tvPingcang;
    private TextView tvRevoke;
    private View tabJcLayout;
    private TextView tabJcText;
    private View tabJcLine;
    private View tabCcLayout;
    private TextView tabCcText;
    private View tabCcLine;
    private View tabRLayout;
    private TextView tabRText;
    private View tabRLine;
    private TradeRecordAdapter mAdapter;
    private DateDialog mDateDialog;
    private String startTime, endTime;
    private int clickId = R.id.tab_jcLayout;


    @Override
    public int getLayoutId() {
        return R.layout.activity_trade_record;
    }

    @Override
    public void initPresenter() {
        mPresenter.init(this);
    }

    @Override
    public void loadData() {
        mIvBack.setVisibility(View.VISIBLE);
        mTitle.setTitle(false, R.color.white, "交易记录");
        mDateDialog = new DateDialog(TradeRecordActivity.this);
        View view = LayoutInflater.from(TradeRecordActivity.this).inflate(R.layout.layout_trade_record_top,null);
        tvStartTime = (TextView) view.findViewById(R.id.tv_startTime);
        tvEndTime = (TextView) view.findViewById(R.id.tv_endTime);
        tvOne = (TextView) view.findViewById(R.id.tv_one);
        tvTwo = (TextView) view.findViewById(R.id.tv_two);
        tvThr = (TextView) view.findViewById(R.id.tv_thr);
        tvFour = (TextView) view.findViewById(R.id.tv_four);
        //风险率
        tvRisk = (TextView) view.findViewById(R.id.tv_risk);
        //建仓手数
        tvJiancang = (TextView) view.findViewById(R.id.tv_jiancang);
        //平仓手数
        tvPingcang = (TextView) view.findViewById(R.id.tv_pingcang);
        //已撤单手数
        tvRevoke = (TextView) view.findViewById(R.id.tv_revoke);
        //建仓
        tabJcLayout = view.findViewById(R.id.tab_jcLayout);
        tabJcText = (TextView) view.findViewById(R.id.tab_jcText);
        tabJcLine = view.findViewById(R.id.tab_jcLine);
        //持仓
        tabCcLayout = view.findViewById(R.id.tab_ccLayout);
        tabCcText = (TextView) view.findViewById(R.id.tab_ccText);
        tabCcLine = view.findViewById(R.id.tab_ccLine);
        //已撤单
        tabRLayout = view.findViewById(R.id.tab_rLayout);
        tabRText = (TextView) view.findViewById(R.id.tab_rText);
        tabRLine = view.findViewById(R.id.tab_rLine);

        tvStartTime.setOnClickListener(this);
        tvEndTime.setOnClickListener(this);
        tabJcLayout.setOnClickListener(this);
        tabCcLayout.setOnClickListener(this);
        tabRLayout.setOnClickListener(this);
        mIvBack.setOnClickListener(this);

        lvListView.addHeaderView(view);
        List<String>  list = new ArrayList<>();
        for (int i = 0 ; i < 30;i++){
            list.add(new String("111"));
        }
        mAdapter = new TradeRecordAdapter(TradeRecordActivity.this);
        lvListView.setAdapter(mAdapter);

        mDateDialog.setBirthdayListener(new DateDialog.OnBirthListener() {
            @Override
            public void onClick(String start, String end) {
                startTime = start;
                endTime = end;
                tvStartTime.setText(start);
                tvEndTime.setText(end);
                mDateDialog.dismiss();
            }
        });
        endTime = mDateDialog.getYear() + "-" + mDateDialog.getMonth() + "-" + mDateDialog.getDay();
        startTime = getStartTime();
        tvStartTime.setText(startTime);
        tvEndTime.setText(endTime);
        mPresenter.getHistoryTradeBean(startTime, SpUtil.getString(Constant.CACHE_TAG_UID),SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN),endTime);
        getHistoryData(clickId);
        refreshView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh(RefreshLayout refreshlayout) {
                getHistoryData(clickId);
            }
        });
        tvTwo.setVisibility(View.INVISIBLE);
        tvThr.setVisibility(View.INVISIBLE);
        tvFour.setText("建仓价");
    }

    private String getStartTime(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar c = Calendar.getInstance();
        //过去七天
        c.setTime(new Date());
        c.add(Calendar.DATE, - 7);
        Date d = c.getTime();
        return format.format(d);
    }

    @Override
    public void onClick(View v) {
      switch (v.getId()){
          case R.id.tv_startTime:
              mDateDialog.show();
              break;
          case R.id.tv_endTime:
              mDateDialog.show();
              break;
          case R.id.tab_jcLayout:
              clickId = v.getId();
              getHistoryData(clickId);
              tabJcText.setTextColor(getResources().getColor(R.color.color_008EFF));
              tabJcLine.setBackgroundResource(R.color.color_008EFF);
              tabJcLine.setVisibility(View.VISIBLE);
              tabCcText.setTextColor(getResources().getColor(R.color.color_333333));
              tabCcLine.setVisibility(View.INVISIBLE);
              tabRText.setTextColor(getResources().getColor(R.color.color_333333));
              tabRLine.setVisibility(View.INVISIBLE);
              tvTwo.setVisibility(View.INVISIBLE);
              tvThr.setVisibility(View.INVISIBLE);
              tvFour.setText("建仓价");
              break;
          case R.id.tab_ccLayout:
              clickId = v.getId();
              getHistoryData(clickId);
              tabJcText.setTextColor(getResources().getColor(R.color.color_333333));
              tabJcLine.setVisibility(View.INVISIBLE);
              tabCcText.setTextColor(getResources().getColor(R.color.color_008EFF));
              tabCcLine.setBackgroundResource(R.color.color_008EFF);
              tabCcLine.setVisibility(View.VISIBLE);
              tabRText.setTextColor(getResources().getColor(R.color.color_333333));
              tabRLine.setVisibility(View.INVISIBLE);
              tvTwo.setVisibility(View.VISIBLE);
              tvTwo.setText("建仓价");
              tvThr.setVisibility(View.VISIBLE);
              tvThr.setText("平仓价");
              tvFour.setText("平仓盈亏");
              break;
          case R.id.tab_rLayout:
              clickId = v.getId();
              getHistoryData(clickId);
              tabCcText.setTextColor(getResources().getColor(R.color.color_333333));
              tabCcLine.setVisibility(View.INVISIBLE);
              tabJcText.setTextColor(getResources().getColor(R.color.color_333333));
              tabJcLine.setVisibility(View.INVISIBLE);
              tabRText.setTextColor(getResources().getColor(R.color.color_008EFF));
              tabRLine.setBackgroundResource(R.color.color_008EFF);
              tabRLine.setVisibility(View.VISIBLE);
              tvTwo.setVisibility(View.INVISIBLE);
              tvThr.setVisibility(View.VISIBLE);
              tvThr.setText("委托类型");
              tvFour.setText("委托价");
              break;
          case R.id.tv_back:
              finish();
              break;
      }
    }

    private void getHistoryData(int id){
        switch (id){
            case R.id.tab_jcLayout:
                mPresenter.getHistoryBuilderBean(startTime, SpUtil.getString(Constant.CACHE_TAG_UID),SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN),endTime);
                break;
            case R.id.tab_ccLayout:
                mPresenter.getHistoryCloseBean(startTime, SpUtil.getString(Constant.CACHE_TAG_UID),SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN),endTime);
                break;
            case R.id.tab_rLayout:
                mPresenter.getHistoryMissBean(startTime, SpUtil.getString(Constant.CACHE_TAG_UID),SpUtil.getString(Constant.CACHE_ACCOUNT_TOKEN),endTime);
                break;
        }
    }

    @Override
    public void bindHistoryTradeBean(HistoryTradeBean bean) {
        tvRisk.setText(bean.profitLoss);
        tvJiancang.setText(bean.open);
        tvPingcang.setText(bean.close);
        tvRevoke.setText(bean.cancel);
    }

    @Override
    public void bindHistoryMissBean(List<HistoryMissPositionBean> list) {
        refreshView.finishRefresh();
        mAdapter.setMList(list);
    }

    @Override
    public void bindHistoryCloseBean(List<HistoryClosePositionBean> list) {
        refreshView.finishRefresh();
        mAdapter.setCList(list);
    }

    @Override
    public void bindHistoryBuilderBean(List<HistoryBuiderPositionBean> list) {
        refreshView.finishRefresh();
        mAdapter.setBList(list);
    }

    @Override
    public void showErrorMsg(String msg, String type) {
        super.showErrorMsg(msg, type);
       refreshView.finishRefresh();
    }
}
