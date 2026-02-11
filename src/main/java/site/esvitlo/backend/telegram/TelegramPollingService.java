package site.esvitlo.backend.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramPollingService {

    private final TelegramClient telegramClient;
    private final TelegramUpdateHandler handler;
    private final ObjectMapper mapper = new ObjectMapper();

    private long offset = 0;

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        try {
            String json = telegramClient.getUpdates(offset);

            // Якщо прийшла порожня відповідь або null
            if (json == null || json.isBlank()) return;

            JsonNode root = mapper.readTree(json);

            // Перевіряємо, чи успішний запит (ok: true)
            if (!root.path("ok").asBoolean(true)) {
                log.error("Telegram Error: {}", root.toPrettyString());
                return;
            }

            for (JsonNode update : root.path("result")) {
                // Оновлюємо offset, щоб не обробляти це повідомлення знову
                offset = update.path("update_id").asLong() + 1;

                JsonNode msg = update.path("message");
                if (msg.isMissingNode()) continue;

                // 1. Логіка для дізнавання ID каналу (якщо переслали повідомлення)
                JsonNode fwd = msg.path("forward_from_chat");
                if (!fwd.isMissingNode()) {
                    Long channelId = fwd.path("id").asLong();
                    Long privateChatId = msg.path("chat").path("id").asLong();
                    String title = fwd.path("title").asText("Channel");

                    telegramClient.sendMessage(
                            privateChatId,
                            "📢 Channel Found!\nID: " + channelId + "\nTitle: " + title
                    );
                    continue;
                }

                // 2. Обробляємо тільки приватні повідомлення
                String chatType = msg.path("chat").path("type").asText();
                if (!"private".equals(chatType)) continue;

                Long chatId = msg.path("chat").path("id").asLong();
                String text = msg.path("text").asText("");

                // Безпечне отримання даних користувача
                Long telegramUserId = msg.path("from").path("id").asLong();
                String username = msg.path("from").path("username").asText("unknown"); // "unknown" краще ніж ""

                if (!text.isBlank()) {
                    try {
                        handler.handlePrivateMessage(chatId, telegramUserId, username, text);
                    } catch (Exception e) {
                        log.error("Error handling message from {}: {}", username, e.getMessage(), e);
                        telegramClient.sendMessage(chatId, "🚨 Сталася внутрішня помилка.");
                    }
                }
            }
        } catch (Exception e) {
            log.error("Polling error: ", e);
        }
    }
}

