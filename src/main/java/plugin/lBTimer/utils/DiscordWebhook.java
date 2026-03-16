package plugin.lBTimer.utils;

import org.bukkit.Bukkit;
import plugin.lBTimer.LBTimer;
import plugin.lBTimer.models.WebhookTemplate;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class DiscordWebhook {

    public static void sendWebhook(WebhookTemplate template, String eventName, String remainingTime) {
        if (template == null || template.getUrl() == null || template.getUrl().isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(LBTimer.getInstance(), () -> {
            try {
                URL url = new URL(template.getUrl());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("User-Agent", "LBTimer-Plugin");
                connection.setDoOutput(true);

                String content = format(template.getContent(), eventName, remainingTime);
                String title = format(template.getTitle(), eventName, remainingTime);
                String description = format(template.getDescription(), eventName, remainingTime);
                String footer = format(template.getFooter(), eventName, remainingTime);
                
                String username = template.getUsername();
                String avatarUrl = template.getImage();

                StringBuilder json = new StringBuilder();
                json.append("{");
                
                if (!username.isEmpty()) json.append("\"username\":\"").append(escape(username)).append("\",");
                if (!avatarUrl.isEmpty()) json.append("\"avatar_url\":\"").append(escape(avatarUrl)).append("\",");
                
                json.append("\"content\":\"").append(escape(content)).append("\",");
                json.append("\"embeds\":[{");
                json.append("\"color\":").append(hexToInt(template.getColor())).append(",");
                
                if (!title.isEmpty()) json.append("\"title\":\"").append(escape(title)).append("\",");
                if (!template.getWebUrl().isEmpty()) json.append("\"url\":\"").append(escape(template.getWebUrl())).append("\",");
                if (!description.isEmpty()) json.append("\"description\":\"").append(escape(description)).append("\",");
                if (!template.getThumbnail().isEmpty()) json.append("\"thumbnail\":{\"url\":\"").append(escape(template.getThumbnail())).append("\"},");
                if (!footer.isEmpty()) json.append("\"footer\":{\"text\":\"").append(escape(footer)).append("\"},");
                
                json.append("\"timestamp\":\"").append(java.time.Instant.now().toString()).append("\"");
                json.append("}]}");

                String jsonStr = json.toString();

                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonStr.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = connection.getResponseCode();
                LBTimer.getInstance().getLogger().info("Webhook Sent. Response Code: " + responseCode);
                if (responseCode < 200 || responseCode >= 300) {
                    LBTimer.getInstance().getLogger().warning("Webhook failed with HTTP response code: " + responseCode);
                    LBTimer.getInstance().getLogger().warning("Payload was: " + jsonStr);
                }

                connection.disconnect();

            } catch (Exception e) {
                LBTimer.getInstance().getLogger().warning("Failed to send Discord webhook: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private static String format(String text, String eventName, String remaining) {
        if (text == null) return "";
        return text.replace("%event%", eventName).replace("%remaining%", remaining);
    }

    private static String escape(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r");
    }

    private static int hexToInt(String colorStr) {
        if (colorStr == null || colorStr.isEmpty()) return 16777215;
        
        // Handle names
        switch (colorStr.toUpperCase()) {
            case "RED": return 16711680;
            case "GREEN": return 65280;
            case "BLUE": return 255;
            case "YELLOW": return 16776960;
            case "CYAN": return 65535;
            case "MAGENTA": return 16711935;
            case "WHITE": return 16777215;
            case "BLACK": return 0;
            case "ORANGE": return 16753920;
        }

        if (colorStr.startsWith("#")) colorStr = colorStr.substring(1);
        try {
            return Integer.parseInt(colorStr, 16);
        } catch (NumberFormatException e) {
            return 16777215;
        }
    }
}
