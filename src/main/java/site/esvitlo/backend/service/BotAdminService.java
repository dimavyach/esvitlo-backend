package site.esvitlo.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.esvitlo.backend.domain.*;
import site.esvitlo.backend.repository.*;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BotAdminService {

    private final DeviceRepository deviceRepo;
    private final ChannelRepository channelRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final DeviceService deviceService;

    // =========================
    // DEVICES
    // =========================

    @Transactional
    public Device createDevice(User user, String name) {
        return deviceService.createDevice(user, name);
    }

    public String buildDevicesList(User user) {

        List<Device> devices =
                deviceRepo.findByUserId(user.getId());

        if (devices.isEmpty()) {
            return """
                   📭 У вас поки що немає пристроїв.
                   
                   ➜ Додайте перший:
                   add_device <назва>
                   """;
        }

        StringBuilder sb = new StringBuilder("📱 Ваші пристрої:\n\n");

        for (Device d : devices) {

            String status = d.isOnline()
                    ? "🟢 ONLINE"
                    : "🔴 OFFLINE";

            sb.append("• ")
                    .append(d.getName())
                    .append(" — ")
                    .append(status)
                    .append("\nKey: ")
                    .append(d.getDeviceKey().substring(0, 8))
                    .append("...\n\n");
        }

        return sb.toString();
    }

    public String buildStatus(User user) {

        List<Device> devices =
                deviceRepo.findByUserId(user.getId());

        if (devices.isEmpty()) {
            return "📭 У вас немає пристроїв.";
        }

        StringBuilder sb = new StringBuilder("📊 Статус світла:\n\n");

        for (Device d : devices) {

            String status = d.isOnline()
                    ? "⚡ Світло є"
                    : "❌ Світло відсутнє";

            sb.append("• ")
                    .append(d.getName())
                    .append(" → ")
                    .append(status)
                    .append("\n");
        }

        return sb.toString();
    }

    // =========================
    // CHANNELS
    // =========================

    @Transactional
    public String addChannel(User user, Long chatId) {

        Channel channel = channelRepo
                .findByChatId(chatId)
                .orElseGet(() -> {
                    Channel ch = new Channel();
                    ch.setChatId(chatId);
                    ch.setUser(user);
                    return channelRepo.save(ch);
                });

        return """
               ✅ Канал додано.
               
               ➜ Тепер привʼяжіть пристрій:
               bind <device_key> <chat_id>
               """;
    }

    // =========================
    // BIND
    // =========================

    @Transactional
    public String bind(User user, Long chatId, String deviceKey) {

        Device device = deviceService.findByKeyOrThrow(deviceKey);

        if (!device.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Цей пристрій вам не належить.");
        }

        Channel channel = channelRepo
                .findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Канал не знайдено."));

        boolean exists = subscriptionRepo
                .findByDeviceAndChannel(device, channel)
                .isPresent();

        if (exists) {
            return "⚠️ Пристрій вже привʼязаний до цього каналу.";
        }

        Subscription sub = new Subscription();
        sub.setDevice(device);
        sub.setChannel(channel);

        subscriptionRepo.save(sub);

        return """
               🔗 Пристрій успішно привʼязано!
               
               Тепер ви будете отримувати повідомлення про зміну статусу.
               """;
    }

    // =========================
    // UNBIND
    // =========================

    @Transactional
    public String unbind(User user, Long chatId, String deviceKey) {

        Device device = deviceService.findByKeyOrThrow(deviceKey);

        if (!device.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Цей пристрій вам не належить.");
        }

        Channel channel = channelRepo
                .findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Канал не знайдено."));

        Subscription sub = subscriptionRepo
                .findByDeviceAndChannel(device, channel)
                .orElseThrow(() ->
                        new IllegalArgumentException("Привʼязку не знайдено.")
                );

        subscriptionRepo.delete(sub);

        return "❌ Пристрій відвʼязано від каналу.";
    }
}

