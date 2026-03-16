# LBTimer - Advanced Minecraft Event Scheduler

LBTimer is a powerful and flexible Minecraft plugin designed to schedule commands and Discord notifications with ease. It supports multiple languages, reusable webhook templates, and sub-tasks for precise timing.

## Features
- **Modular Configuration:** Split configuration files for tasks, webhooks, and general settings.
- **Webhook Templates:** Create reusable Discord webhook designs in `webhooks.yml`.
- **Sub-Tasks:** Schedule commands or webhooks to fire before or after the main event (e.g., countdowns).
- **Multi-Language Support:** Easily switch between languages with a dedicated folder-based system.
- **In-Game GUI:** View upcoming events and remaining time via a sleek graphical interface.
- **PlaceholderAPI Support:** Display event names and timers anywhere on your server.
- **Smart Shutdown Recovery:** Ensures Discord webhooks are sent even if the server is shutting down.

## Commands
- `/lbtimer` - Main help menu.
- `/lbtimer events` - Opens the event list GUI.
- `/lbtimer reload` - Reloads all configuration files and tasks.
- `/lbtimer testwebhook <event_id>` - Manually trigger a webhook for testing.

## Configuration

### `tasks.yml`
Define your events with custom materials, lore, and times. support for specific days of the week.
```yaml
Tasks:
  boss_event:
    name: "&cEjder Boss"
    time: "20:00:00"
    day: [SATURDAY, SUNDAY]
    material: DRAGON_EGG
    commands:
      - "mm mobs spawn ender_dragon"
    sub_tasks:
      - time: -300
        webhook: alert-template
        commands: ["broadcast &cBoss spawning in 5 minutes!"]
```

### `webhooks.yml`
Create templates for Discord notifications with thumbnails, hex colors, and placeholders.
```yaml
alert-template:
  url: "YOUR_WEBHOOK_URL"
  username: "Server Alerts"
  title: "%event% Starting!"
  description: "The event starts in %remaining%."
  color: "RED"
  thumbnail: "https://yourserver.com/logo.png"
```

## Placeholders
- `%lbtimer_event%` - Next upcoming event name.
- `%lbtimer_time%` - Time remaining for the next event.
- `%lbtimer_<event_id>%` - Time remaining for a specific event.

## Installation
1. Download the latest release.
2. Place the `.jar` in your `plugins/` folder.
3. Restart the server.
4. Configure `tasks.yml` and `webhooks.yml` to your liking.
