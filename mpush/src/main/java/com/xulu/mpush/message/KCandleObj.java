package com.xulu.mpush.message;

import java.io.Serializable;

/**
 * Created by fangzhu
 */
public class KCandleObj implements Serializable {
    /*时间str 类型，格式化之后用来显示X轴的时间*/
    private String time;
    /*时间long 类型*/
    private long timeLong;
    /*id唯一标示*/
    private String id;
    /*开盘价*/
    private double open;
    /*最高价*/
    private double high;
    /*最低价*/
    private double low;
    /*收盘价*/
    private double close;
    /*成交量*/
    private double vol;
    /*持仓量*/
    private double hold;
    /*作为标记 当成是没有设置颜色值的判断*/
    public static final int COLOR_VOL_ENABLE = -100;
    /*成交量  线条颜色 1=红色 0=绿色*/
    private int color = COLOR_VOL_ENABLE;
    /*主图均线值*/
    private double normValue;

    //开盘时间
    private String startTime;
    //停盘时间
    private String endTime;
    //交易中间休盘时间
    private String middleTime;
    // 总成交量 用来计算周期内成交量
    private double totalVol;
    // 请求列表时间 用来计算周期内成交量
    private long  reqTime;


    public KCandleObj (){}

    public KCandleObj(double normValue) {
        this.normValue = normValue;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimeLong() {
        return timeLong;
    }

    public void setTimeLong(long timeLong) {
        this.timeLong = timeLong;
    }

    public double getOpen() {
        return open;
    }

    public void setOpen(double open) {
        this.open = open;
    }

    public double getHigh() {
        return high;
    }

    public void setHigh(double high) {
        this.high = high;
    }

    public double getLow() {
        return low;
    }

    public void setLow(double low) {
        this.low = low;
    }

    public double getClose() {
        return close;
    }

    public void setClose(double close) {
        this.close = close;
    }

    public double getVol() {
        return vol;
    }

    public void setVol(double vol) {
        this.vol = vol;
    }

    public double getNormValue() {
        return normValue;
    }

    public void setNormValue(double normValue) {
        this.normValue = normValue;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getMiddleTime() {
        return middleTime;
    }

    public void setMiddleTime(String middleTime) {
        this.middleTime = middleTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public double getHold() {
        return hold;
    }

    public void setHold(double hold) {
        this.hold = hold;
    }

    public double getTotalVol() {
        return totalVol;
    }

    public void setTotalVol(double totalVol) {
        this.totalVol = totalVol;
    }

    public long getReqTime() {
        return reqTime;
    }

    public void setReqTime(long reqTime) {
        this.reqTime = reqTime;
    }
}
