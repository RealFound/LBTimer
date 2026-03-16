package plugin.lBTimer.models;

public class WebhookTemplate {
    private final String url;
    private final String username;
    private final String image;
    private final String thumbnail;
    private final String title;
    private final String description;
    private final String footer;
    private final String content;
    private final String color;
    private final String webUrl;

    public WebhookTemplate(String url, String username, String image, String thumbnail,
                           String title, String description, String footer,
                           String content, String color, String webUrl) {
        this.url = url;
        this.username = username;
        this.image = image;
        this.thumbnail = thumbnail;
        this.title = title;
        this.description = description;
        this.footer = footer;
        this.content = content;
        this.color = color;
        this.webUrl = webUrl;
    }

    public String getUrl() { return url; }
    public String getUsername() { return username; }
    public String getImage() { return image; }
    public String getThumbnail() { return thumbnail; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getFooter() { return footer; }
    public String getContent() { return content; }
    public String getColor() { return color; }
    public String getWebUrl() { return webUrl; }
}
