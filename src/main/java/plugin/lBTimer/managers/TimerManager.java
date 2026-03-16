package plugin.lBTimer.managers;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import plugin.lBTimer.LBTimer;
import plugin.lBTimer.models.SubTask;
import plugin.lBTimer.models.TimerEvent;
import plugin.lBTimer.models.WebhookTemplate;
import plugin.lBTimer.utils.DiscordWebhook;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

public class TimerManager {

    private final LBTimer plugin;
    private BukkitTask task;
    private final Random random = new Random();

    public TimerManager(LBTimer plugin) {
        this.plugin = plugin;
    }

    public void start() {
        stop();
        // Calculate initial times
        for (TimerEvent event : plugin.getConfigManager().getTimerEvents().values()) {
            calculateNextOccurrence(event);
        }

        task = Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 20L, 20L);
    }

    public void stop() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
            task = null;
        }
    }

    private void tick() {
        long now = System.currentTimeMillis();
        for (TimerEvent event : plugin.getConfigManager().getTimerEvents().values()) {
            long target = event.getTargetEpochMilli();
            if (target <= 0) continue;

            long latestTaskTime = target;
            for (SubTask st : event.getSubTasks()) {
                long stTime = target + (st.getTimeOffset() * 1000L);
                if (stTime > latestTaskTime) latestTaskTime = stTime;
            }

            // Main event execution
            if (now >= target && !event.isExecuted()) {
                executeEvent(event);
            }

            // Sub tasks execution
            for (SubTask st : event.getSubTasks()) {
                long stTime = target + (st.getTimeOffset() * 1000L);
                if (now >= stTime && !st.isExecuted()) {
                    executeSubTask(event, st);
                }
            }

            // Recalculate if finished
            if (event.isExecuted() && now >= latestTaskTime + 5000L) {
                boolean allSubExecuted = true;
                for (SubTask st : event.getSubTasks()) {
                    if (!st.isExecuted()) {
                        allSubExecuted = false;
                        break;
                    }
                }
                
                if (allSubExecuted) {
                    calculateNextOccurrence(event);
                }
            }
        }
    }

    private void executeEvent(TimerEvent event) {
        event.setExecuted(true);

        // Run webhook
        String templateName = event.getWebhookTemplateName();
        if (templateName != null && !templateName.isEmpty()) {
            WebhookTemplate template = plugin.getConfigManager().getWebhookTemplates().get(templateName);
            if (template != null) {
                DiscordWebhook.sendWebhook(template, event.getName(), "");
            } else {
                plugin.getLogger().warning("Webhook template '" + templateName + "' not found in webhooks.yml!");
            }
        }

        // Run commands with delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (String cmd : event.getCommands()) {
                String formattedCmd = cmd.replace("%event%", event.getName());
                
                if (formattedCmd.startsWith("[MESSAGE] ")) {
                    String message = ConfigManager.color(formattedCmd.substring(10));
                    Bukkit.broadcastMessage(message);
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ConfigManager.color(formattedCmd));
                }
            }
        }, 20L);
    }

    private void executeSubTask(TimerEvent event, SubTask st) {
        st.setExecuted(true);

        // Run webhook
        String templateName = st.getWebhookTemplateName();
        if (templateName != null && !templateName.isEmpty()) {
            WebhookTemplate template = plugin.getConfigManager().getWebhookTemplates().get(templateName);
            if (template != null) {
                String remaining = formatRemaining(Math.abs(st.getTimeOffset()));
                DiscordWebhook.sendWebhook(template, event.getName(), remaining);
            } else {
                plugin.getLogger().warning("SubTask webhook template '" + templateName + "' not found in webhooks.yml!");
            }
        }

        // Run commands with delay
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            String remainingPlaceHolder = formatRemaining(Math.abs(st.getTimeOffset()));
            for (String cmd : st.getCommands()) {
                String formattedCmd = cmd.replace("%event%", event.getName())
                                         .replace("%remaining%", remainingPlaceHolder);

                if (formattedCmd.startsWith("[MESSAGE] ")) {
                    String message = ConfigManager.color(formattedCmd.substring(10));
                    Bukkit.broadcastMessage(message);
                } else {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), ConfigManager.color(formattedCmd));
                }
            }
        }, 20L);
    }

    private String formatRemaining(int totalSeconds) {
        ConfigManager cm = plugin.getConfigManager();
        if (totalSeconds <= 0) return cm.timeNow;

        int days = totalSeconds / 86400;
        int hours = (totalSeconds % 86400) / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        StringBuilder sb = new StringBuilder();
        if (days > 0) sb.append(days).append(cm.timeDays).append(" ");
        if (hours > 0) sb.append(hours).append(cm.timeHours).append(" ");
        if (minutes > 0) sb.append(minutes).append(cm.timeMinutes).append(" ");
        if (seconds > 0 || sb.length() == 0) sb.append(seconds).append(cm.timeSeconds);

        return sb.toString().trim();
    }

    private void calculateNextOccurrence(TimerEvent event) {
        if (event.getDays().isEmpty()) {
            event.setTargetEpochMilli(-1);
            return;
        }

        String timeStr = event.getTimeString();
        LocalTime timeToRun = null;

        try {
            if (timeStr.contains("-")) {
                String[] parts = timeStr.split("-");
                LocalTime minTime = parseTime(parts[0]);
                LocalTime maxTime = parseTime(parts[1]);

                int minSeconds = minTime.toSecondOfDay();
                int maxSeconds = maxTime.toSecondOfDay();

                if (minSeconds > maxSeconds) {
                    int temp = minSeconds;
                    minSeconds = maxSeconds;
                    maxSeconds = temp;
                }

                int randomSecond = minSeconds + random.nextInt(maxSeconds - minSeconds + 1);
                timeToRun = LocalTime.ofSecondOfDay(randomSecond);
            } else {
                timeToRun = parseTime(timeStr);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid time format for event: " + event.getId() + " - " + event.getTimeString());
            event.setTargetEpochMilli(-1);
            return;
        }

        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ZonedDateTime bestTarget = null;

        for (int i = 0; i <= 7; i++) {
            ZonedDateTime checkDate = now.plusDays(i);
            if (event.getDays().contains(checkDate.getDayOfWeek())) {
                ZonedDateTime candidate = checkDate.with(timeToRun);
                if (candidate.isBefore(now)) continue;
                
                if (bestTarget == null || candidate.isBefore(bestTarget)) {
                    bestTarget = candidate;
                }
            }
        }

        if (bestTarget != null) {
            event.setTargetEpochMilli(bestTarget.toInstant().toEpochMilli());
            event.setExecuted(false);
            for (SubTask st : event.getSubTasks()) st.setExecuted(false);
            plugin.getLogger().info("Event '" + event.getId() + "' scheduled for: " + bestTarget);
        } else {
            event.setTargetEpochMilli(-1);
        }
    }

    private LocalTime parseTime(String s) {
        if (!s.contains(":")) {
            int totalSeconds = Integer.parseInt(s);
            return LocalTime.of(totalSeconds / 3600, (totalSeconds % 3600) / 60, totalSeconds % 60);
        } else {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("H:m:s"));
        }
    }
}
