package se.yifan.android.encprovider;

import android.util.Log;

/**
 * User: robert
 * Date: 26/03/13
 */
public class TimeLogClient {
    private static final String TIMELOG_TAG = "EncTimeLog";

    public static long startTime;
    public static long endTime;
    public static long duration;

    public static long getStartTime() {
        return startTime;
    }

    public static void setStartTime() {
        TimeLogClient.startTime = System.currentTimeMillis();
    }

    public static long getEndTime() {
        return endTime;
    }

    public static void setEndTime() {
        TimeLogClient.endTime = System.currentTimeMillis();
    }

    public static long getDuration() {
        duration = endTime - startTime;
        return duration;
    }

    public static void logClientDuration(String executionPosition) {
        setEndTime();
        Log.i(TIMELOG_TAG, "Total Time spend in "
                + executionPosition + ": "
                + TimeLogClient.getDuration());
    }
}
