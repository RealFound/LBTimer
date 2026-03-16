package plugin.lBTimer.menu;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import plugin.lBTimer.LBTimer;
import plugin.lBTimer.managers.ConfigManager;
import plugin.lBTimer.models.TimerEvent;
import plugin.lBTimer.utils.TimeUtils;

import java.util.ArrayList;
import java.util.List;

public class EventsMenu implements Listener {

    public static void openMenu(Player player) {
        ConfigManager config = LBTimer.getInstance().getConfigManager();
        String title = config.menuTitle;

        int size = 27;
        int eventsCount = config.getTimerEvents().size();
        if (eventsCount > 9) size = 36;
        if (eventsCount > 18) size = 45;
        if (eventsCount > 27) size = 54;

        Inventory inventory = Bukkit.createInventory(null, size, title);
        long now = System.currentTimeMillis();

        if (config.getTimerEvents().isEmpty()) {
            ItemStack noEvents = new ItemStack(Material.BARRIER);
            ItemMeta meta = noEvents.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(config.noEvents);
                noEvents.setItemMeta(meta);
            }
            inventory.setItem(13, noEvents);
        } else {
            for (TimerEvent event : config.getTimerEvents().values()) {
                Material mat;
                try {
                    mat = Material.valueOf(event.getMaterial().toUpperCase());
                } catch (Exception e) {
                    mat = Material.PAPER;
                }
                
                ItemStack item = new ItemStack(mat);
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(event.getName());
                    List<String> lore = new ArrayList<>();
                    
                    // Add config lore
                    if (event.getLore() != null) {
                        lore.addAll(event.getLore());
                    }
                    
                    lore.add(""); // Spacer
                    
                    long target = event.getTargetEpochMilli();
                    if (target > 0) {
                        long diff = target - now;
                        if (diff > 0) {
                            lore.add(ConfigManager.color("&7Kalan Süre: &e" + TimeUtils.formatTime(diff)));
                        } else {
                            lore.add(ConfigManager.color("&7Kalan Süre: &a" + config.timeNow));
                        }
                    } else {
                        lore.add(ConfigManager.color("&cŞu anlık planlanmadı."));
                    }
                    
                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }
                inventory.addItem(item);
            }
        }

        player.openInventory(inventory);
    }

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        String title = LBTimer.getInstance().getConfigManager().menuTitle;
        if (event.getView().getTitle().equals(title)) {
            event.setCancelled(true);
        }
    }
}
