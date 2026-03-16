package plugin.lBTimer.models;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Set;

public class TimerEvent {
    private final String id;
    private final String name;
    private final String timeString;
    private final Set<DayOfWeek> days;
    
    private final boolean announce;
    private final List<String> lore;
    private final String material;
    
    private final String webhookTemplateName;
    private final List<String> commands;
    private final List<SubTask> subTasks;

    private long targetEpochMilli;
    private boolean executed = false;

    public TimerEvent(String id, String name, String timeString, Set<DayOfWeek> days,
                      boolean announce, List<String> lore, String material,
                      String webhookTemplateName, List<String> commands, List<SubTask> subTasks) {
        this.id = id;
        this.name = name;
        this.timeString = timeString;
        this.days = days;
        this.announce = announce;
        this.lore = lore;
        this.material = material;
        this.webhookTemplateName = webhookTemplateName;
        this.commands = commands;
        this.subTasks = subTasks;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getTimeString() { return timeString; }
    public Set<DayOfWeek> getDays() { return days; }
    
    public boolean isAnnounce() { return announce; }
    public List<String> getLore() { return lore; }
    public String getMaterial() { return material; }
    
    public String getWebhookTemplateName() { return webhookTemplateName; }
    public List<String> getCommands() { return commands; }
    public List<SubTask> getSubTasks() { return subTasks; }

    public long getTargetEpochMilli() { return targetEpochMilli; }
    public void setTargetEpochMilli(long targetEpochMilli) { this.targetEpochMilli = targetEpochMilli; }

    public boolean isExecuted() { return executed; }
    public void setExecuted(boolean executed) { this.executed = executed; }
}
