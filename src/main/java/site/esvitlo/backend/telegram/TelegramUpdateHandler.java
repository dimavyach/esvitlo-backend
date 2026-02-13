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
        // 1. Отримуємо або створюємо користувача
        User user = userService.getOrCreate(telegramUserId, username);

        if (text == null || text.isBlank()) {
            telegramClient.sendMessage(chatId, "🤖 Я отримав порожнє повідомлення.", getMainMenu());
            return;
        }

        String rawText = text.trim();

        // 2. Нормалізація (видаляємо емодзі та зайві символи для перевірки кнопок)
        String normalized = rawText.toLowerCase()
                .replace("📱", "")
                .replace("📊", "")
                .replace("➕", "")
                .replace("✏️", "")
                .replace("📢", "")
                .replace("❓", "")
                .replace("🔓", "")
                .replace("🔗", "")
                .replace("👋", "")
                .trim();

        if (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }

        try {
            // 3. Етап 1: Обробка кнопок меню та простих команд
            switch (normalized) {
                case "start" -> {
                    telegramClient.sendMessage(chatId, """
                            👋 Вітаю в Esvitlo Monitor!
                            
                            Цей бот допоможе моніторити наявність світла.
                            
                            📌 Як почати:
                            1. Натисніть Add Device, щоб отримати ключ.
                            2. Налаштуйте ваш пристрій на цей ключ.
                            3. Додайте канал (Add Channel), куди слати сповіщення.
                            4. Привʼяжіть пристрій до каналу (Bind).
                            
                            👇 Оберіть дію в меню:
                            """, getMainMenu());
                    return;
                }

                case "devices", "my devices" -> {
                    telegramClient.sendMessage(chatId, adminService.buildDevicesList(user));
                    return;
                }

                case "status" -> {
                    telegramClient.sendMessage(chatId, adminService.buildStatus(user));
                    return;
                }

                case "help" -> {
                    telegramClient.sendMessage(chatId, buildHelpMessage(), getMainMenu());
                    return;
                }

                // --- КНОПКИ ДІЙ (Інструкції) ---

                case "add device" -> {
                    telegramClient.sendMessage(chatId, """
                            ✏️ Створення пристрою
                            
                            Щоб додати новий пристрій, введіть команду:
                            /add_device НАЗВА
                            
                            Приклад:
                            /add_device Кухня
                            """);
                    return;
                }

                case "add channel" -> {
                    telegramClient.sendMessage(chatId, """
                            📢 Додавання каналу
                            
                            1. Додайте цього бота в ваш канал як адміністратора.
                            2. Напишіть будь-яке повідомлення в канал.
                            3. Перешліть це повідомлення сюди боту.
                            4. Бот скаже ID каналу.
                            5. Введіть команду:
                            /add_channel ID_КАНАЛУ
                            """);
                    return;
                }

                case "bind" -> {
                    telegramClient.sendMessage(chatId, """
                            🔗 Привʼязка пристрою
                            
                            Щоб отримувати сповіщення в канал, зʼєднайте їх командою:
                            /bind КЛЮЧ_ПРИСТРОЮ ID_КАНАЛУ
                            
                            (Ключ беріть в меню My Devices, а ID каналу - після додавання каналу)
                            """);
                    return;
                }

                case "unbind" -> {
                    telegramClient.sendMessage(chatId, """
                            🔓 Відвʼязка пристрою
                            
                            Щоб вимкнути сповіщення для каналу:
                            /unbind КЛЮЧ_ПРИСТРОЮ ID_КАНАЛУ
                            """);
                    return;
                }
            }

            // 4. Етап 2: Обробка команд з аргументами
            String[] parts = rawText.split("\\s+");
            String cmd = parts[0].toLowerCase();
            if (cmd.startsWith("/")) cmd = cmd.substring(1);

            String payload = "";
            if (parts.length > 1) {
                payload = rawText.substring(parts[0].length()).trim();
            }

            switch (cmd) {

                case "add_device" -> {
                    if (payload.isEmpty()) {
                        telegramClient.sendMessage(chatId, "⚠️ Вкажіть назву пристрою.\nПриклад: /add_device Дім");
                        return;
                    }
                    var device = adminService.createDevice(user, payload);
                    telegramClient.sendMessage(chatId,
                            """
                            ✅ Пристрій створено!
                            
                            📱 Назва: %s
                            🔑 Ключ:
                            %s
                            
                            (Скопіюйте цей ключ у прошивку)
                            """.formatted(device.getName(), device.getDeviceKey()));
                }

                case "add_channel" -> {
                    if (payload.isEmpty()) {
                        telegramClient.sendMessage(chatId, "⚠️ Вкажіть ID каналу.\nПриклад: /add_channel -100...");
                        return;
                    }
                    try {
                        Long channelId = Long.parseLong(payload);
                        String reply = adminService.addChannel(user, channelId);
                        telegramClient.sendMessage(chatId, reply);
                    } catch (NumberFormatException e) {
                        telegramClient.sendMessage(chatId, "❌ ID каналу має бути числом.");
                    }
                }

                case "bind" -> {
                    String[] args = payload.split("\\s+");
                    if (args.length < 2) {
                        telegramClient.sendMessage(chatId, """
                                ⚠️ Помилка формату
                                
                                Потрібно вказати ключ та ID.
                                Приклад:
                                /bind 550e8400-e29b... -100123456...
                                """);
                        return;
                    }

                    String deviceKey = args[0];
                    try {
                        Long channelId = Long.parseLong(args[1]);
                        String reply = adminService.bind(user, channelId, deviceKey);
                        telegramClient.sendMessage(chatId, reply);
                    } catch (NumberFormatException e) {
                        telegramClient.sendMessage(chatId, "❌ ID каналу має бути числом.");
                    }
                }

                case "unbind" -> {
                    String[] args = payload.split("\\s+");
                    if (args.length < 2) {
                        telegramClient.sendMessage(chatId, """
                                ⚠️ Помилка формату
                                
                                Потрібно вказати ключ та ID.
                                Приклад:
                                /unbind 550e8400-e29b... -100123456...
                                """);
                        return;
                    }

                    String deviceKey = args[0];
                    try {
                        Long channelId = Long.parseLong(args[1]);
                        String reply = adminService.unbind(user, channelId, deviceKey);
                        telegramClient.sendMessage(chatId, reply);
                    } catch (NumberFormatException e) {
                        telegramClient.sendMessage(chatId, "❌ ID каналу має бути числом.");
                    }
                }

                // Дублюємо команди без аргументів (на випадок ручного вводу)
                case "devices" -> telegramClient.sendMessage(chatId, adminService.buildDevicesList(user));
                case "status" -> telegramClient.sendMessage(chatId, adminService.buildStatus(user));

                default -> // Якщо команда не знайдена, і це не було натискання відомої кнопки
                        telegramClient.sendMessage(chatId, """
                                🤷 Невідома команда.
                                Спробуйте /help або скористайтесь меню.
                                """, getMainMenu());
            }

        } catch (IllegalArgumentException e) {
            telegramClient.sendMessage(chatId, "❌ Помилка: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error processing telegram update", e);
            telegramClient.sendMessage(chatId, "🚨 Сталася технічна помилка.");
        }
    }

    private String buildHelpMessage() {
        return """
                📖 Список команд:
                
                /add_device [назва] - додати пристрій
                /add_channel [id] - додати канал
                /bind [ключ] [id] - увімкнути сповіщення
                /unbind [ключ] [id] - вимкнути сповіщення
                
                /devices - мої пристрої
                /status - перевірити світло
                """;
    }

    private Map<String, Object> getMainMenu() {
        return Map.of(
                "keyboard", List.of(
                        List.of(
                                Map.of("text", "➕ Add Device"),
                                Map.of("text", "📢 Add Channel")
                        ),
                        List.of(
                                Map.of("text", "🔗 Bind"),
                                Map.of("text", "🔓 Unbind")
                        ),
                        List.of(
                                Map.of("text", "📱 My Devices"),
                                Map.of("text", "📊 Status")
                        ),
                        List.of(
                                Map.of("text", "❓ Help")
                        )
                ),
                "resize_keyboard", true,
                "is_persistent", true
        );
    }
}