package plugin.lBTimer.models;

import java.util.List;

public class SubTask {
    private final int timeOffset;
    private final String webhookTemplateName;
    private final List<String> commands;
    private boolean executed = false;

    public SubTask(int timeOffset, String webhookTemplateName, List<String> commands) {
        this.timeOffset = timeOffset;
        this.webhookTemplateName = webhookTemplateName;
        this.commands = commands;
    }

    public int getTimeOffset() { return timeOffset; }
    public String getWebhookTemplateName() { return webhookTemplateName; }
    public List<String> getCommands() { return commands; }

    public boolean isExecuted() { return executed; }
    public void setExecuted(boolean executed) { this.executed = executed; }
}
