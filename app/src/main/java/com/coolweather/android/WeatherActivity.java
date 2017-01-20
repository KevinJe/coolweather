package com.coolweather.android;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.coolweather.android.gson.Forecast;
import com.coolweather.android.gson.HourlyForecast;
import com.coolweather.android.gson.Weather;
import com.coolweather.android.util.HttpUtil;
import com.coolweather.android.util.Utility;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {
    private static final String TAG = "WeatherActivity";
    private ScrollView mWeatherLayout;
    private TextView mTitleCity;
    private TextView mTitleUpdateTime;
    private TextView mDegree;
    private TextView mWeatherInfoText;
    private LinearLayout mForecastLayout;
    private TextView mAqiText;
    private TextView mPm25Text;
    private TextView mComfortText;
    private TextView mCarWashText;
    private TextView mSportText;
    private LinearLayout mHourlyForecastLayout;
    private ImageView mBingPicImage;
    public SwipeRefreshLayout mSwipeRefresh;
    private HorizontalScrollView mHorizontalScrollView;
    public  DrawerLayout mDrawerLayout;
    private Button mNavButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //在真机测试发现以下不能实现沉浸式状态栏的体验
        /*if (Build.VERSION.SDK_INT >= 21) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN|
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        setContentView(R.layout.activity_weather);
        //初始化各个控件
        mWeatherLayout = (ScrollView) findViewById(R.id.weather_layout);
        mHorizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontal_scrollview);
        mTitleCity = (TextView) findViewById(R.id.title_city);
        mTitleUpdateTime = (TextView) findViewById(R.id.title_update_time);
        mDegree = (TextView) findViewById(R.id.degree_text);
        mWeatherInfoText = (TextView) findViewById(R.id.weather_info_text);
        mForecastLayout = (LinearLayout) findViewById(R.id.forecast_layout);
        mAqiText = (TextView) findViewById(R.id.aqi_text);
        mPm25Text = (TextView) findViewById(R.id.pm25_text);
        mComfortText = (TextView) findViewById(R.id.comfort_text);
        mCarWashText = (TextView) findViewById(R.id.car_wash_text);
        mSportText = (TextView) findViewById(R.id.sport_text);
        mHourlyForecastLayout = (LinearLayout) findViewById(R.id.hourly_forecast_layout);
        mBingPicImage = (ImageView) findViewById(R.id.bing_pic_img);
        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.weather_refresh);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavButton = (Button) findViewById(R.id.nav_button);
        mSwipeRefresh.setColorSchemeResources(R.color.colorPrimary,R.color.colorAccent,R.color.colorRefresh);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherString = prefs.getString("weather", null);
        final String weatherId;
        if (weatherString != null) {
            //有缓存时直接解析数据
            Weather weather = Utility.handleWeatherResponse(weatherString);
            weatherId = weather.basic.id;
            showWeatherInfo(weather);
        } else {
            //无缓存时去服务器查询天气
            weatherId = getIntent().getStringExtra("weather_id");
            mWeatherLayout.setVisibility(View.INVISIBLE);
            mHorizontalScrollView.setVisibility(View.INVISIBLE);
            requestWeather(weatherId);
        }
        String bing_pic = prefs.getString("bing_pic", null);
        if (bing_pic != null) {
            Glide.with(this).load(bing_pic).into(mBingPicImage);
        } else {
            loadBingPic();
        }
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                requestWeather(weatherId);
            }
        });
        mNavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        String requestBingPic = "http://guolin.tech/api/bing_pic";
        HttpUtil.sendOKHttpRequest(requestBingPic, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.
                        getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Glide.with(WeatherActivity.this).load(bingPic).into(mBingPicImage);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * 根据天气id请求城市天气信息
     *
     * @param weatherId
     */
    public void requestWeather(String weatherId) {
        String weatherUrl = "http://guolin.tech/api/weather?cityid="
                + weatherId + "&key=1f4e0fc644244bce998db78b1781f66c";
        HttpUtil.sendOKHttpRequest(weatherUrl, new Callback() {
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                final String responseText = response.body().string();
                final Weather weather = Utility.handleWeatherResponse(responseText);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (weather != null && "ok".equals(weather.status)) {
                            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                            editor.putString("weather", responseText);
                            editor.apply();
                            showWeatherInfo(weather);
                        } else {
                            Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        }
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }

            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                        mSwipeRefresh.setRefreshing(false);
                    }
                });
            }
        });
        loadBingPic();
    }

    /**
     * 处理并展示weather实体类中的数据
     *
     * @param weather
     */
    private void showWeatherInfo(Weather weather) {
//        String cityName = weather.basic.cityName;
//        String updateTime = weather.basic.update.updateTime.split(" ")[1];
        String cityName = weather.basic.city;
        String updateTime = weather.basic.update.loc;
        String degree = weather.now.tmp + "°";
        String weatherInfo = weather.now.cond.txt + "   " + weather.now.wind.dir;
//        Log.d(TAG, "showWeatherInfo: "+weather.now.tmp);
//        Log.d(TAG, "showWeatherInfo: "+weather.now.cond.txt);
        mTitleCity.setText(cityName);
        mTitleUpdateTime.setText(updateTime + " 更新");
        mDegree.setText(degree);
        mWeatherInfoText.setText(weatherInfo);

        mHourlyForecastLayout.removeAllViews();
        for (HourlyForecast hourlyForecast : weather.hourlyForecast) {
            View view = LayoutInflater.from(this).inflate
                    (R.layout.hourly_forecast_item, mHourlyForecastLayout, false);
            TextView hourlyTime = (TextView) view.findViewById(R.id.hourly_update_time);
            TextView hourlyWeather = (TextView) view.findViewById(R.id.hourly_weather_info);
            TextView hourlyWind = (TextView) view.findViewById(R.id.hourly_wind_info);
            TextView hourlyTmp = (TextView) view.findViewById(R.id.hourly_tmp);
            hourlyTime.setText(hourlyForecast.date.split(" ")[1]);
            hourlyWeather.setText(hourlyForecast.cond.txt);
            hourlyWind.setText(hourlyForecast.wind.dir);
            hourlyTmp.setText(hourlyForecast.tmp + " °");
            mHourlyForecastLayout.addView(view);
            Log.d(TAG, "showWeatherInfo: " + weather.hourlyForecast.size());
        }


        mForecastLayout.removeAllViews();
        for (Forecast forecast : weather.forecastList) {
            View view = LayoutInflater.from(this).inflate(R.layout.forecast_item, mForecastLayout, false);
            TextView dateText = (TextView) view.findViewById(R.id.date_text);
            TextView infoText = (TextView) view.findViewById(R.id.info_text);
            TextView maxText = (TextView) view.findViewById(R.id.max_text);
            TextView minText = (TextView) view.findViewById(R.id.min_text);
            dateText.setText(forecast.date);
            infoText.setText(forecast.cond.txt_d + " " + forecast.wind.dir);
            maxText.setText(forecast.tmp.max + "°");
            minText.setText(forecast.tmp.min + "°");
            mForecastLayout.addView(view);
        }
        if (weather.aqi != null) {
            mAqiText.setText(weather.aqi.city.aqi);
            mPm25Text.setText(weather.aqi.city.pm25);
        }
        String comfort = "舒适度： " + weather.suggestion.comf.txt;
        String carWash = "洗车指数： " + weather.suggestion.cw.txt;
        String sport = "运动指数： " + weather.suggestion.sport.txt;
        mComfortText.setText(comfort);
        mCarWashText.setText(carWash);
        mSportText.setText(sport);
        mWeatherLayout.setVisibility(View.VISIBLE);
        mHorizontalScrollView.setVisibility(View.VISIBLE);
    }
}
