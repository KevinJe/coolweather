package com.coolweather.android.gson;

/**
 * Created by Kevin on 2017/1/20.
 */

public class Forecast {
    /*public String date;
    @SerializedName("tmp")
    public Temperture temperture;
    @SerializedName("cond")
    public More more;

    public class Temperture {
        public String max;
        public String min;
    }

    public class More {
        @SerializedName("txt_d")
        public String info;
    }*/

    public String date;
    public Cond cond;
    public Wind wind;
    public Tmp tmp;

    public class Tmp {
        public String max;
        public String min;
    }

    public class Wind {
        public String dir;
    }

    public class Cond {
        public String txt_d;
    }
}
