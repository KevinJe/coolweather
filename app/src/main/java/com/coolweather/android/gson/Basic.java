package com.coolweather.android.gson;

/**
 * Created by Kevin on 2017/1/20.
 */

public class Basic {
    /*@SerializedName("city")
    public String cityName;
    @SerializedName("id")
    public String weatherId;
    public Update update;
    public class Update{
        @SerializedName("loc")
        public String updateTime;
    }*/
    public String city;
    public String id;
    public Update update;
    public class Update{
        public String loc;
    }
}
