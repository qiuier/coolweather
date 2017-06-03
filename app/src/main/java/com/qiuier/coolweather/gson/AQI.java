package com.qiuier.coolweather.gson;

/**
 * Created by qiuyi on 2017/6/3.
 * 空气质量
 */

public class AQI {
    public AQICity city;
    public class AQICity{
        public String aqi;
        public String pm25;
    }
}
