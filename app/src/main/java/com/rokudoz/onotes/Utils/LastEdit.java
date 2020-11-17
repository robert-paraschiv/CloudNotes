package com.rokudoz.onotes.Utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LastEdit {
    private static final int SECOND_MILLIS = 1000;
    private static final int MINUTE_MILLIS = 60 * SECOND_MILLIS;
    private static final int HOUR_MILLIS = 60 * MINUTE_MILLIS;
    private static final int DAY_MILLIS = 24 * HOUR_MILLIS;
    private static final int WEEK_MILLIS = 7 * DAY_MILLIS;

    public LastEdit() {
    }

    public static String getLastEdit(long time) {
        if (time < 1000000000000L) {
            // if timestamp given in seconds, convert to millis
            time *= 1000;
        }
        Date timeStampDate = new Date(time);
        DateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        DateFormat dayFormat = new SimpleDateFormat("E", Locale.getDefault());
        DateFormat dateFormat = new SimpleDateFormat("HH:mm, dd-MMM-yyyy", Locale.getDefault());

        long now = System.currentTimeMillis() + 30000;
        if (time > now || time <= 0) {
            hourFormat.format(now);
        }


        // TODO: localize
        final long diff = now - time;
        if (diff < 24 * HOUR_MILLIS && dayFormat.format(now).equals(dayFormat.format(timeStampDate))) {
            return "Today at " + hourFormat.format(timeStampDate);
        } else if (diff < 48 * HOUR_MILLIS) {
            return "Yesterday at " + hourFormat.format(timeStampDate);
        } else if (diff < 7 * DAY_MILLIS) {
            return "" + dayFormat.format(timeStampDate) + " at " + hourFormat.format(timeStampDate);
        } else if (diff < 3 * WEEK_MILLIS) {
            return "" + diff / DAY_MILLIS + " days ago";
        } else {
            return "" + dateFormat.format(timeStampDate);
        }
    }
}
