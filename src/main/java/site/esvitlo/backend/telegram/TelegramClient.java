package site.esvitlo.backend.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.HashMap;
import java.util.Map;

@Component
public class TelegramClient {

    private final RestClient restClient;
    private final String botToken;

    public TelegramClient(
            @Value("${telegram.bot-token}") String botToken
    ) {
        this.botToken = botToken;
        this.restClient = RestClient.create("https://api.telegram.org");
    }

    // Старий метод (для звичайних повідомлень)
    public void sendMessage(Long chatId, String text) {
        sendMessage(chatId, text, null);
    }

    // НОВИЙ метод (з підтримкою кнопок)
    public void sendMessage(Long chatId, String text, Object replyMarkup) {
        Map<String, Object> body = new HashMap<>();
        body.put("chat_id", chatId);
        body.put("text", text);

        if (replyMarkup != null) {
            body.put("reply_markup", replyMarkup);
        }

        restClient.post()
                .uri("/bot{token}/sendMessage", botToken)
                .body(body) // Spring автоматично перетворить Map у JSON
                .retrieve()
                .toBodilessEntity();
    }

    public String getUpdates(long offset) {
        return restClient.get()
                .uri("/bot{token}/getUpdates?timeout=30&offset={offset}",
                        botToken, offset)
                .retrieve()
                .body(String.class);
    }

}

