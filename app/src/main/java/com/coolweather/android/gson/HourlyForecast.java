package com.coolweather.android.gson;

/**
 * Created by Kevin on 2017/1/20.
 */

public class HourlyForecast {
    public String date;
    public String tmp;
    public Cond cond;
    public Wind wind;

    public class Cond {
        public String txt;
    }

    public class Wind {
        public String dir;
    }
}
