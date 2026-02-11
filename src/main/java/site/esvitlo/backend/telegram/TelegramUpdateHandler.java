package site.esvitlo.backend.telegram;


import org.springframework.stereotype.Component;
import site.esvitlo.backend.domain.Device;
import site.esvitlo.backend.service.BotAdminService;
import site.esvitlo.backend.service.DeviceService;

@Component
public class TelegramUpdateHandler {

    private final BotAdminService adminService;
    private final BotCommandParser parser;
    private final TelegramClient telegramClient;
    private final DeviceService deviceService;


    public TelegramUpdateHandler(BotAdminService adminService,
                                 BotCommandParser parser,
                                 TelegramClient telegramClient, DeviceService deviceService) {
        this.adminService = adminService;
        this.parser = parser;
        this.telegramClient = telegramClient;
        this.deviceService = deviceService;
    }

    public void handlePrivateMessage(Long chatId, String text) {

        String[] parts = parser.parse(text);
        if (parts.length == 0) return;

        String cmd = parts[0];

        try {
            switch (cmd) {

                case "add_channel" -> {
                    if (parts.length != 2) {
                        telegramClient.sendMessage(
                                chatId,
                                "Формат: add_channel <chat_id>"
                        );
                        return;
                    }

                    Long channelChatId = Long.parseLong(parts[1]);
                    telegramClient.sendMessage(
                            chatId,
                            adminService.addChannel(channelChatId)
                    );
                }

                case "bind" -> {
                    if (parts.length != 3) {
                        telegramClient.sendMessage(
                                chatId,
                                "Формат: bind <chat_id> <device_key>"
                        );
                        return;
                    }

                    Long channelChatId = Long.parseLong(parts[1]);
                    String deviceKey = parts[2];

                    telegramClient.sendMessage(
                            chatId,
                            adminService.bind(channelChatId, deviceKey)
                    );
                }

                case "add_device" -> {
                    if (parts.length < 2) {
                        telegramClient.sendMessage(chatId, "Формат: add_device <name>");
                        return;
                    }

                    String name = parts[1];
                    Device device = deviceService.createDevice(name);

                    telegramClient.sendMessage(
                            chatId,
                            "📱 Пристрій створено\nName: " + device.getName() +
                                    "\nKey:\n" + device.getDeviceKey()
                    );
                }

                default -> telegramClient.sendMessage(
                        chatId,
                        "Невідома команда.\nДоступні:\nadd_channel\nbind"
                );
            }
        }catch (Exception e) {
            telegramClient.sendMessage(chatId, "Помилка: " + e.getMessage());
        }
    }
}

