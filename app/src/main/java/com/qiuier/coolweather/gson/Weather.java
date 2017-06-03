package com.qiuier.coolweather.gson;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by qiuyi on 2017/6/3.
 */

public class Weather {
    public String status;//返回天气数据中包含一项status 成功“ok” 失败--"失败代码"

    public Basic basic;

    public AQI aqi;

    public Now now;

    public Suggestion suggestion;

    @SerializedName("daily_forecast")
    public List<Forecast> forecastList;
}
