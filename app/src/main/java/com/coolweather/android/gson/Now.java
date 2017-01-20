package com.coolweather.android.gson;

/**
 * Created by Kevin on 2017/1/20.
 */

public class Now {
    /*@SerializedName("tmp")
    public String temperature;
    @SerializedName("cond")
    public More more;
    public class More{
        @SerializedName("txt")
        public String info;
    }*/

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
