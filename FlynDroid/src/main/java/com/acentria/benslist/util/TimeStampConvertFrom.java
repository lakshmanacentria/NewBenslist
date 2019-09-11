package com.acentria.benslist.util;

import android.text.format.DateFormat;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

public class TimeStampConvertFrom {
    private static String TAG = "TimeStampConvertFrom=> ";

    public static String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        Log.e(TAG, "Time  " + time);
        if (time / 1000000000000L >= 1)
            cal.setTimeInMillis(time);
        else
            cal.setTimeInMillis(time * 1000);

        String date = DateFormat.format("dd-MM-yyyy hh:mm aa", cal).toString();
        Log.e("TimeStampConvertFrom=> ", date);
        return date;
    }
}
