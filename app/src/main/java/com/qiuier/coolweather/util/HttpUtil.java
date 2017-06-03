package com.qiuier.coolweather.util;

import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * Created by qiuyi on 2017/6/2.
 */

public class HttpUtil {
    //发送http请求，处理服务器响应
    public static void sendOkHttpRequest(String address,okhttp3.Callback callback){
        OkHttpClient client=new OkHttpClient();
        Request request=new Request.Builder().url(address).build();
        client.newCall(request).enqueue(callback);
    }
}
