package plugin.lBTimer;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import plugin.lBTimer.commands.TimerCommand;
import plugin.lBTimer.hooks.PAPIExpansion;
import plugin.lBTimer.managers.ConfigManager;
import plugin.lBTimer.managers.TimerManager;
import plugin.lBTimer.menu.EventsMenu;

public final class LBTimer extends JavaPlugin {
    
    private static LBTimer instance;
    private ConfigManager configManager;
    private TimerManager timerManager;

    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        configManager.loadConfig();

        timerManager = new TimerManager(this);
        timerManager.start();

        if (getCommand("lbtimer") != null) {
            getCommand("lbtimer").setExecutor(new TimerCommand());
        }
        getServer().getPluginManager().registerEvents(new EventsMenu(), this);

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPIExpansion(this).register();
        }

        getLogger().info("LBTimer has been enabled.");
    }

    @Override
    public void onDisable() {
        if (timerManager != null) {
            timerManager.stop();
        }
        getLogger().info("LBTimer has been disabled.");
    }

    public static LBTimer getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public TimerManager getTimerManager() {
        return timerManager;
    }
}
