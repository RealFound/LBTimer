package plugin.lBTimer.utils;

import plugin.lBTimer.LBTimer;
import plugin.lBTimer.managers.ConfigManager;

public class TimeUtils {
    public static String formatTime(long millis) {
        if (millis <= 0) {
            return LBTimer.getInstance().getConfigManager().timeNow;
        }

        long seconds = millis / 1000;
        long days = seconds / 86400;
        seconds %= 86400;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;

        ConfigManager config = LBTimer.getInstance().getConfigManager();
        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append(" ").append(config.timeDays).append(" ");
        if (hours > 0) sb.append(hours).append(" ").append(config.timeHours).append(" ");
        if (minutes > 0) sb.append(minutes).append(" ").append(config.timeMinutes).append(" ");

        if (days == 0 && hours == 0 && minutes == 0) {
            sb.append(seconds).append(" ").append(config.timeSeconds);
        } else if (seconds > 0) {
            sb.append(seconds).append(" ").append(config.timeSeconds);
        }

        return sb.toString().trim();
    }
}
