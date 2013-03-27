package se.yifan.android.encprovider;

/**
 * User: robert
 * Date: 26/03/13
 */
public class TimeLogServer {
    public static long startTime;
    public static long endTime;
    public static long duration;

    public static long getStartTime() {
        return startTime;
    }

    public static void setStartTime() {
        TimeLogServer.startTime = System.currentTimeMillis();
    }

    public static long getEndTime() {
        return endTime;
    }

    public static void setEndTime() {
        TimeLogServer.endTime = System.currentTimeMillis();
    }

    public static long getDuration() {
        duration = endTime - startTime;
        return duration;
    }

    public static void logServerDuration(String executionPosition) {
        setEndTime();
        ServerHandlerThread.logger.info("Total Time spend in "
                + executionPosition + ": "
                + TimeLogServer.getDuration());
    }
}
