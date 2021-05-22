package com.dragoonssoft.apps.caronacap;

import android.app.Application;
import android.content.Context;

/**
 * Created by wiler on 03/08/2017.
 */

public class GetTimeAgo extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time, Context context) {
        if(time < 1000000000000L){
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if(time > now || time <= 0){
            return null;
        }

        final long diff = now - time;
        if(diff < MINUTE_MILLIS){
            return "Agora mesmo";
        }else if(diff < 2 * MINUTE_MILLIS){
            return "Há 1 minuto";
        }else if(diff < 50 * MINUTE_MILLIS){
            return diff/MINUTE_MILLIS + " minutos atrás";
        }else if(diff < 90 * MINUTE_MILLIS){
            return "1 hora atrás";
        }else if(diff < 24 * HOUR_MILLIS){
            return diff/HOUR_MILLIS + " horas atrás";
        }else if(diff < 48 * HOUR_MILLIS){
            return "Ontem";
        }else{
            return diff/DAY_MILLIS + "dias atrás";
        }

    }

}
