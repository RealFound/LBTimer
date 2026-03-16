package plugin.lBTimer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import plugin.lBTimer.LBTimer;
import plugin.lBTimer.managers.ConfigManager;
import plugin.lBTimer.menu.EventsMenu;
import plugin.lBTimer.models.TimerEvent;
import plugin.lBTimer.models.WebhookTemplate;
import plugin.lBTimer.utils.DiscordWebhook;

public class TimerCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        ConfigManager config = LBTimer.getInstance().getConfigManager();

        if (args.length == 0) {
            if (!sender.hasPermission("tasktimer.help")) {
                sender.sendMessage(config.noPermission);
                return true;
            }
            for (String line : config.commandsHelp) {
                sender.sendMessage(line);
            }
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "events":
            case "show":
                if (!sender.hasPermission("lbtimer.events") && !sender.hasPermission("lbtimer.show")) {
                    sender.sendMessage(config.noPermission);
                    return true;
                }
                if (sender instanceof Player player) {
                    EventsMenu.openMenu(player);
                } else {
                    sender.sendMessage("This command is for players only.");
                }
                break;
            case "reload":
                if (!sender.hasPermission("lbtimer.reload")) {
                    sender.sendMessage(config.noPermission);
                    return true;
                }
                LBTimer.getInstance().getTimerManager().stop();
                config.loadConfig();
                LBTimer.getInstance().getTimerManager().start();
                sender.sendMessage(config.reloaded);
                break;
            case "testwebhook":
                if (!sender.hasPermission("lbtimer.admin")) {
                    sender.sendMessage(config.noPermission);
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(ConfigManager.color("&cKullanım: /lbtimer testwebhook <event_id>"));
                    return true;
                }
                String eventId = args[1];
                TimerEvent timerEvent = config.getTimerEvents().get(eventId);
                if (timerEvent == null) {
                    sender.sendMessage(ConfigManager.color("&cBöyle bir etkinlik bulunamadı!"));
                    return true;
                }
                
                String templateName = timerEvent.getWebhookTemplateName();
                if (templateName == null || templateName.isEmpty()) {
                    sender.sendMessage(ConfigManager.color("&cBu etkinliğe atanmış bir webhook şablonu yok!"));
                    return true;
                }
                
                WebhookTemplate template = config.getWebhookTemplates().get(templateName);
                if (template == null) {
                    sender.sendMessage(ConfigManager.color("&cAtanan şablon (" + templateName + ") webhooks.yml içinde bulunamadı!"));
                    return true;
                }

                sender.sendMessage(ConfigManager.color("&aWebhook (" + templateName + ") gönderiliyor..."));
                DiscordWebhook.sendWebhook(template, timerEvent.getName(), "0sn");
                break;
            default:
                if (sender.hasPermission("tasktimer.help")) {
                    for (String line : config.commandsHelp) {
                        sender.sendMessage(line);
                    }
                } else {
                    sender.sendMessage(config.noPermission);
                }
                break;
        }

        return true;
    }
}
