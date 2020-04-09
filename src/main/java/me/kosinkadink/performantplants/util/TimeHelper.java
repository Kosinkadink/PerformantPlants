package me.kosinkadink.performantplants.util;

public class TimeHelper {

    public static long secondsToTicks(int seconds) {
        return seconds * 20;
    }

    public static long minutesToTicks(int minutes) {
        return secondsToTicks(minutes * 60);
    }

    public static long millisToTicks(long millis) {
        return (long)((millis/1000.0) * 20);
    }

}
