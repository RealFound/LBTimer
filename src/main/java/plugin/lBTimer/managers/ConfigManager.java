package plugin.lBTimer.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import plugin.lBTimer.LBTimer;
import plugin.lBTimer.models.SubTask;
import plugin.lBTimer.models.TimerEvent;
import plugin.lBTimer.models.WebhookTemplate;

import java.io.File;
import java.time.DayOfWeek;
import java.util.*;

public class ConfigManager {
    private final LBTimer plugin;

    public String prefix;
    public String noPermission;
    public String reloaded;
    public String menuTitle;
    public String noEvents;
    public List<String> commandsHelp;

    public String timeDays;
    public String timeHours;
    public String timeMinutes;
    public String timeSeconds;
    public String timeNow;

    private final Map<String, WebhookTemplate> webhookTemplates = new HashMap<>();
    private final Map<String, TimerEvent> timerEvents = new LinkedHashMap<>();

    public ConfigManager(LBTimer plugin) {
        this.plugin = plugin;
    }

    public void loadConfig() {
        // Save default resources if they don't exist
        saveResourceIfNotExists("tasks.yml");
        saveResourceIfNotExists("webhooks.yml");
        saveResourceIfNotExists("lang/tr.yml");
        saveResourceIfNotExists("lang/en.yml");
        plugin.saveDefaultConfig();
        plugin.reloadConfig();

        FileConfiguration config = plugin.getConfig();
        String lang = config.getString("language", "tr");

        // Load Language
        loadLanguage(lang);

        // Load Webhooks
        loadWebhooks();

        // Load Tasks
        loadTasks();
    }

    private void saveResourceIfNotExists(String resourcePath) {
        File file = new File(plugin.getDataFolder(), resourcePath);
        if (!file.exists()) {
            plugin.saveResource(resourcePath, false);
        }
    }

    private void loadLanguage(String lang) {
        File langFile = new File(plugin.getDataFolder(), "lang/" + lang + ".yml");
        if (!langFile.exists()) {
            langFile = new File(plugin.getDataFolder(), "lang/tr.yml");
        }
        FileConfiguration langConfig = YamlConfiguration.loadConfiguration(langFile);

        prefix = color(langConfig.getString("messages.prefix", "&8[&eLBTimer&8] &r"));
        noPermission = color(langConfig.getString("messages.no_permission", "&cBuna yetkiniz yok!"));
        reloaded = color(langConfig.getString("messages.reloaded", "&aEklenti başarıyla yeniden yüklendi!"));
        menuTitle = color(langConfig.getString("messages.menu_title", "&8Etkinlikler"));
        noEvents = color(langConfig.getString("messages.no_events", "&cŞu anda planlanmış bir etkinlik bulunmuyor."));
        commandsHelp = colorList(langConfig.getStringList("messages.commands_help"));

        timeDays = langConfig.getString("time_format.days", "g");
        timeHours = langConfig.getString("time_format.hours", "s");
        timeMinutes = langConfig.getString("time_format.minutes", "dk");
        timeSeconds = langConfig.getString("time_format.seconds", "sn");
        timeNow = color(langConfig.getString("time_format.now", "Şimdi!"));
    }

    private void loadWebhooks() {
        webhookTemplates.clear();
        File file = new File(plugin.getDataFolder(), "webhooks.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);

        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;

            WebhookTemplate template = new WebhookTemplate(
                    section.getString("url", ""),
                    section.getString("username", "LBTimer"),
                    section.getString("image", ""),
                    section.getString("thumbnail", ""),
                    section.getString("title", ""),
                    section.getString("description", ""),
                    section.getString("footer", ""),
                    section.getString("content", ""),
                    section.getString("color", "WHITE"),
                    section.getString("webUrl", "")
            );
            webhookTemplates.put(key, template);
        }
    }

    private void loadTasks() {
        timerEvents.clear();
        File file = new File(plugin.getDataFolder(), "tasks.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        ConfigurationSection tasksSection = config.getConfigurationSection("Tasks");
        if (tasksSection == null) tasksSection = config.getConfigurationSection("tasks");
        if (tasksSection == null) return;

        for (String key : tasksSection.getKeys(false)) {
            ConfigurationSection section = tasksSection.getConfigurationSection(key);
            if (section == null) continue;

            String name = color(section.getString("name", key));
            String timeString = section.getString("time", "12:00:00");
            
            List<String> daysStr = section.getStringList("day");
            if (daysStr.isEmpty()) daysStr = section.getStringList("days"); // fallback
            
            Set<DayOfWeek> days = new HashSet<>();
            for (String day : daysStr) {
                try {
                    days.add(DayOfWeek.valueOf(day.toUpperCase()));
                } catch (IllegalArgumentException ignored) {}
            }

            boolean announce = section.getBoolean("announce", true);
            List<String> lore = colorList(section.getStringList("lore"));
            String material = section.getString("material", "CLOCK");
            String webhookTemplate = section.getString("webhook", "");

            List<String> commands = section.getStringList("commands");

            List<SubTask> subTasksList = new ArrayList<>();
            List<?> subTasksRaw = section.getList("sub_tasks");
            if (subTasksRaw != null) {
                for (Object st : subTasksRaw) {
                    if (st instanceof Map<?, ?> stMap) {
                        try {
                            int timeOffset = Integer.parseInt(stMap.get("time").toString());
                            String stWh = stMap.containsKey("webhook") ? String.valueOf(stMap.get("webhook")) : "";
                            
                            List<String> stCommands = new ArrayList<>();
                            Object cmdsObj = stMap.get("commands");
                            if (cmdsObj instanceof List<?> cmds) {
                                for(Object cmd : cmds) stCommands.add(String.valueOf(cmd));
                            }
                            
                            subTasksList.add(new SubTask(timeOffset, stWh, stCommands));
                        } catch (Exception ignored) {}
                    }
                }
            }

            TimerEvent timerEvent = new TimerEvent(key, name, timeString, days,
                    announce, lore, material, webhookTemplate, commands, subTasksList);

            timerEvents.put(key, timerEvent);
        }
    }

    public Map<String, TimerEvent> getTimerEvents() { return timerEvents; }
    public Map<String, WebhookTemplate> getWebhookTemplates() { return webhookTemplates; }

    public static String color(String s) {
        if (s == null) return "";
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', s);
    }

    public static List<String> colorList(List<String> list) {
        if (list == null) return new ArrayList<>();
        List<String> colored = new ArrayList<>();
        for (String s : list) colored.add(color(s));
        return colored;
    }
}
