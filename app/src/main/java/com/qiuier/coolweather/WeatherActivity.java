package com.qiuier.coolweather;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.qiuier.coolweather.gson.Forecast;
import com.qiuier.coolweather.gson.Weather;
import com.qiuier.coolweather.util.HttpUtil;
import com.qiuier.coolweather.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private ScrollView weatherLayout;//滚动条
    private TextView titleCity;//标题城市
    private TextView titleUpdateTime;//标题更新时间

    private TextView degreeText;//气温
    private TextView weatherInfoText;//天气情况

    private LinearLayout forecastLayout;//七天天气
    private TextView aqiText;//aqi指数
    private TextView pm25Text;

    private TextView comfortText;//舒适度
    private TextView carWashText;//洗车建议
    private TextView sportText;//运动建议

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        initControl();

        SharedPreferences prfes= PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString=prfes.getString("weather",null);
        if (weatherString!=null){
            Weather weather= Utility.handleWeatherResponse(weatherString);
            showWeatherInfo(weather);
        }else {
            String weatherId=getIntent().getStringExtra("weather_id");
            weatherLayout.setVisibility(View.INVISIBLE);//先将所有控件隐藏
            requestWeather(weatherId);

        }
    }

    private void requestWeather(String weatherId) {
        String wetherUrl="http://guolin.tech/api/weather?cityid=" + weatherId +"&key=3df3008faa1d4a1991481c45807bd79f";
        HttpUtil.sendOkHttpRequest(wetherUrl, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气失败", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)){
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather",responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        }else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }

    private void showWeatherInfo(Weather weather) {
        String cityName = weather.basic.cityName;
        String updateTime=weather.basic.update.updateTime.split(" ")[1];//取时间的2017-06-03 15:51 后半部分
        String degree=weather.now.temperature+"℃";//气温
        String weatherInfo=weather.now.more.info;//天气情况
        titleCity.setText(cityName);
        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        weatherInfoText.setText(weatherInfo);

        forecastLayout.removeAllViews();
        for (Forecast f:weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item,forecastLayout,false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText= (TextView) view.findViewById(R.id.info_text);
            TextView maxText= (TextView) view.findViewById(R.id.max_text);
            TextView minText= (TextView) view.findViewById(R.id.min_text);
            dateText.setText(f.date);
            infoText.setText(f.more.info);
            maxText.setText(f.temperature.max);
            minText.setText(f.temperature.min);

            forecastLayout.addView(view);
        }
        if (weather.aqi!=null){
            aqiText.setText(weather.aqi.city.aqi);
            pm25Text.setText(weather.aqi.city.pm25);
        }

        String comfort = "舒适度："+weather.suggestion.comfort.info;
        String carWash="洗车指数："+weather.suggestion.carWash.info;
        String sport="运动指数"+weather.suggestion.sport.info;
        comfortText.setText(comfort);
        carWashText.setText(carWash);
        sportText.setText(sport);

        weatherLayout.setVisibility(View.VISIBLE);
    }

    private void initControl() {
        weatherLayout= (ScrollView) findViewById(R.id.weather_layout);
        titleCity= (TextView) findViewById(R.id.title_city);
        titleUpdateTime= (TextView) findViewById(R.id.title_update_time);
        degreeText= (TextView) findViewById(R.id.degree_text);
        weatherInfoText= (TextView) findViewById(R.id.weather_info_text);
        forecastLayout= (LinearLayout) findViewById(R.id.forecast_layout);
        aqiText= (TextView) findViewById(R.id.aqi_text);
        pm25Text= (TextView) findViewById(R.id.pm25_text);
        comfortText= (TextView) findViewById(R.id.comfort_text);
        carWashText= (TextView) findViewById(R.id.car_wash_text);
        sportText= (TextView) findViewById(R.id.sport_text);
    }
}
