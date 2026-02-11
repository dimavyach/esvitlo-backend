package site.esvitlo.backend.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

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

    public void sendMessage(Long chatId, String text) {
        restClient.post()
                .uri("/bot{token}/sendMessage?chat_id={chatId}&text={text}",
                        botToken, chatId, text)
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

