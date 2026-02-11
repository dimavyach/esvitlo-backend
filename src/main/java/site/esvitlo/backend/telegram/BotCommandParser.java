package site.esvitlo.backend.telegram;

import org.springframework.stereotype.Component;

@Component
public class BotCommandParser {

    public String[] parse(String text) {
        return text.trim().split("\\s+");
    }
}
