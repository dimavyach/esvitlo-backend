package site.esvitlo.backend.telegram;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import site.esvitlo.backend.domain.User;
import site.esvitlo.backend.service.BotAdminService;
import site.esvitlo.backend.service.UserService;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramUpdateHandler {

    private final TelegramClient telegramClient;
    private final BotAdminService adminService;
    private final UserService userService;

    public void handlePrivateMessage(
            Long chatId,
            Long telegramUserId,
            String username,
            String text
    ) {
        User user = userService.getOrCreate(telegramUserId, username);

        if (text == null || text.isBlank()) {
            telegramClient.sendMessage(chatId, "🤖 Я отримав порожнє повідомлення.");
            return;
        }

        String rawText = text.trim();

        // Нормалізація
        String normalized = rawText.toLowerCase()
                .replace("📱", "")
                .replace("📊", "")
                .replace("➕", "")
                .replace("✏️", "")
                .replace("📢", "")
                .replace("❓", "")
                .replace("🔓", "")
                .replace("👋", "")
                .trim();

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        try {
            switch (normalized) {
                case "start" -> {
                    // ТУТ ми передаємо кнопки третім параметром
                    telegramClient.sendMessage(chatId, """
                            👋 Вітаю в Esvitlo Monitor!
                            
                            📌 Як почати:
                            1️⃣ Додайте пристрій
                            2️⃣ Додайте канал
                            3️⃣ Привʼяжіть пристрій
                            
                            👇 Меню з кнопками має з'явитися внизу.
                            """, getMainMenu());
                    return;
                }

                // ... інші case залишаються без змін ...

                case "devices", "my devices" -> {
                    telegramClient.sendMessage(chatId, adminService.buildDevicesList(user));
                    return;
                }
                case "status" -> {
                    telegramClient.sendMessage(chatId, adminService.buildStatus(user));
                    return;
                }
                case "help" -> {
                    telegramClient.sendMessage(chatId, buildHelpMessage(), getMainMenu()); // Можна і тут показати кнопки
                    return;
                }

                // ... решта коду (add_device, unbind і т.д.) ...

                // Кнопки меню
                case "add device" -> {
                    telegramClient.sendMessage(chatId, """
                            ✏️ <b>Додавання пристрою</b>
                            
                            Напишіть команду:
                            <code>add_device Назва</code>
                            """);
                    return;
                }
                case "add channel" -> {
                    telegramClient.sendMessage(chatId, """
                            📢 <b>Додавання каналу</b>
                            
                            1️⃣ Додайте бота в канал.
                            2️⃣ Напишіть сюди:
                            <code>add_channel ID_КАНАЛУ</code>
                            """);
                    return;
                }
            }

            // ... Блок обробки команд з аргументами (add_device <name>) залишається тим самим ...
            String[] parts = rawText.split("\\s+");
            String cmd = parts[0].toLowerCase();
            if (cmd.startsWith("/")) cmd = cmd.substring(1);

            String payload = "";
            if (parts.length > 1) {
                payload = rawText.substring(parts[0].length()).trim();
            }

            switch (cmd) {
                // ... ваші команди add_device, bind і т.д. ...
                case "add_device" -> {
                    if (payload.isEmpty()) {
                        telegramClient.sendMessage(chatId, "⚠️ Вкажіть назву.");
                        return;
                    }
                    var device = adminService.createDevice(user, payload);
                    telegramClient.sendMessage(chatId, "✅ Створено: " + device.getName() + "\nKey: " + device.getDeviceKey());
                }
                // ... і так далі ...

                default -> telegramClient.sendMessage(chatId, "🤷 Невідома команда. Натисніть /start");
            }

        } catch (Exception e) {
            log.error("Error", e);
            telegramClient.sendMessage(chatId, "🚨 Помилка: " + e.getMessage());
        }
    }

    private String buildHelpMessage() {
        return """
                📖 Допомога
                /add_device [назва]
                /devices
                /status
                """;
    }

    // 👇 МЕТОД ДЛЯ СТВОРЕННЯ КЛАВІАТУРИ
    private Map<String, Object> getMainMenu() {
        return Map.of(
                "keyboard", List.of(
                        // Рядок 1
                        List.of(
                                Map.of("text", "➕ Add Device"),
                                Map.of("text", "📢 Add Channel")
                        ),
                        // Рядок 2
                        List.of(
                                Map.of("text", "📱 My Devices"),
                                Map.of("text", "📊 Status")
                        ),
                        // Рядок 3
                        List.of(
                                Map.of("text", "❓ Help")
                        )
                ),
                "resize_keyboard", true, // Робить кнопки компактними
                "is_persistent", true    // Залишає меню постійним
        );
    }
}