package site.esvitlo.backend.telegram;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class TelegramPollingService {

    private final TelegramClient telegramClient;
    private final TelegramUpdateHandler handler;
    private final ObjectMapper mapper = new ObjectMapper();

    private long offset = 0;

    public TelegramPollingService(TelegramClient telegramClient,
                                  TelegramUpdateHandler handler) {
        this.telegramClient = telegramClient;
        this.handler = handler;
    }

    @Scheduled(fixedDelay = 3000)
    public void poll() {
        try {
            String json = telegramClient.getUpdates(offset);
            JsonNode root = mapper.readTree(json);

            for (JsonNode update : root.path("result")) {
                offset = update.path("update_id").asLong() + 1;

                JsonNode msg = update.path("message");
                if (msg.isMissingNode()) continue;

                JsonNode fwd = msg.path("forward_from_chat");
                if (!fwd.isMissingNode()) {
                    Long channelId = fwd.path("id").asLong();
                    Long privateChatId = msg.path("chat").path("id").asLong();

                    telegramClient.sendMessage(
                            privateChatId,
                            "Chat ID: " + channelId
                    );
                    continue;
                }

                String chatType = msg.path("chat").path("type").asText();
                if (!"private".equals(chatType)) continue;

                Long chatId = msg.path("chat").path("id").asLong();
                String text = msg.path("text").asText("");

                if (!text.isBlank()) {
                    handler.handlePrivateMessage(chatId, text);
                }
            }
        } catch (Exception e) {
            // логувати за бажанням
        }
    }
}

