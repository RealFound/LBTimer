package plugin.lBTimer.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import plugin.lBTimer.LBTimer;
import plugin.lBTimer.managers.ConfigManager;
import plugin.lBTimer.models.TimerEvent;
import plugin.lBTimer.utils.TimeUtils;

public class PAPIExpansion extends PlaceholderExpansion {

    private final LBTimer plugin;

    public PAPIExpansion(LBTimer plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "lbtimer";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().isEmpty() ? "Author" : plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        ConfigManager config = plugin.getConfigManager();
        long now = System.currentTimeMillis();

        if (params.equalsIgnoreCase("event")) {
            TimerEvent nextEvent = getNextEvent();
            if (nextEvent != null) {
                return nextEvent.getName();
            }
            return config.noEvents;
        }

        if (params.equalsIgnoreCase("time")) {
            TimerEvent nextEvent = getNextEvent();
            if (nextEvent != null) {
                long target = nextEvent.getTargetEpochMilli();
                if (target > now) {
                    return TimeUtils.formatTime(target - now);
                } else {
                    return config.timeNow;
                }
            }
            return "-";
        }

        // %lbtimer_<id>% -> time left for specific event
        TimerEvent specificEvent = config.getTimerEvents().get(params);
        if (specificEvent != null) {
            long target = specificEvent.getTargetEpochMilli();
            if (target > now) {
                return TimeUtils.formatTime(target - now);
            } else {
                return config.timeNow;
            }
        }

        return null; // Not matched
    }

    private TimerEvent getNextEvent() {
        TimerEvent next = null;
        long now = System.currentTimeMillis();
        for (TimerEvent event : plugin.getConfigManager().getTimerEvents().values()) {
            long target = event.getTargetEpochMilli();
            if (target <= 0) continue;
            if (target >= now && (next == null || target < next.getTargetEpochMilli())) {
                next = event;
            }
        }
        return next;
    }
}
