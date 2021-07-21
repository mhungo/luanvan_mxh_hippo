package vn.edu.stu.Util;

import android.app.Application;
import android.content.Context;

import vn.edu.stu.luanvanmxhhippo.R;

public class GetTimeAgo extends Application {

    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;

    public static String getTimeAgo(long time, Context ctx) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }

        long now = System.currentTimeMillis();
        if (time > now || time <= 0) {
            return null;
        }

        String just = ctx.getString(R.string.justnow);
        String aminuteago = ctx.getString(R.string.aminuteago);
        String minuteago = ctx.getString(R.string.minuteago);
        String ahourago = ctx.getString(R.string.anhourago);
        String hourago = ctx.getString(R.string.hourago);
        String yesterday = ctx.getString(R.string.yesterday);
        String daysago = ctx.getString(R.string.daysago);

        final long diff = now - time;
        if (diff < MINUTE_MILLIS) {
            return just;
        } else if (diff < 2 * MINUTE_MILLIS) {
            return aminuteago;
        } else if (diff < 50 * MINUTE_MILLIS) {
            return diff / MINUTE_MILLIS + " " + minuteago;
        } else if (diff < 90 * MINUTE_MILLIS) {
            return ahourago;
        } else if (diff < 24 * HOUR_MILLIS) {
            return diff / HOUR_MILLIS + " " + hourago;
        } else if (diff < 48 * HOUR_MILLIS) {
            return yesterday;
        } else {
            return diff / DAY_MILLIS + " " + daysago;
        }
    }
}