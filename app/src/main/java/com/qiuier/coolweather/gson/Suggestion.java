package com.qiuier.coolweather.gson;

import com.google.gson.annotations.SerializedName;

/**
 * Created by qiuyi on 2017/6/3.
 */

public class Suggestion {

    @SerializedName("comf")
    public Comfort comfort;

    @SerializedName("cw")
    public CarWash carWash;

    @SerializedName("sport")
    public Sport sport;

    public class Comfort{//舒适度指数
        @SerializedName("txt")
        public String info;
    }
    public class CarWash{//洗车指数
        @SerializedName("txt")
        public String info;
    }
    public class Sport{//运动指数
        @SerializedName("txt")
        public String info;
    }
}
