package site.esvitlo.backend.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.esvitlo.backend.domain.*;
import site.esvitlo.backend.repository.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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
        List<Device> devices = deviceRepo.findByUserId(user.getId());

        if (devices.isEmpty()) {
            return """
                   📭 У вас поки що немає пристроїв.
                   
                   ➜ Додайте перший через меню Add Device.
                   """;
        }

        StringBuilder sb = new StringBuilder("📱 Ваші пристрої:\n\n");

        for (Device d : devices) {
            String status = d.isOnline() ? "🟢 ONLINE" : "🔴 OFFLINE";

            // Отримуємо список каналів, куди цей пристрій шле сповіщення
            List<Subscription> subs = subscriptionRepo.findByDevice_Id(d.getId());
            String linkedChannels = subs.isEmpty()
                    ? "немає"
                    : subs.stream()
                    .map(s -> String.valueOf(s.getChannel().getChatId()))
                    .collect(Collectors.joining(", "));

            sb.append("========================\n")
                    .append("Назва: ").append(d.getName()).append("\n")
                    .append("Статус: ").append(status).append("\n")
                    .append("Ключ: ").append(d.getDeviceKey()).append("\n")
                    .append("Канали: ").append(linkedChannels).append("\n");
        }
        sb.append("========================");

        return sb.toString();
    }

    public String buildStatus(User user) {
        List<Device> devices = deviceRepo.findByUserId(user.getId());
        if (devices.isEmpty()) return "📭 У вас немає пристроїв.";

        StringBuilder sb = new StringBuilder("📊 Статус світла:\n\n");

        for (Device d : devices) {
            String status = d.isOnline() ? "⚡ Світло Є" : "❌ Світла НЕМАЄ";
            String timeAgo = formatDuration(d.getLastSeen());

            sb.append(status).append(" — ").append(d.getName()).append("\n")
                    .append("🕒 Оновлено: ").append(timeAgo).append("\n\n");
        }

        return sb.toString();
    }

    // =========================
    // CHANNELS
    // =========================

    @Transactional
    public String addChannel(User user, Long chatId) {
        channelRepo.findByChatId(chatId)
                .orElseGet(() -> createNewChannel(user, chatId));

        return "✅ Канал додано і готовий до роботи.";
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

        // Автоматично створюємо канал, якщо його немає
        Channel channel = channelRepo.findByChatId(chatId)
                .orElseGet(() -> createNewChannel(user, chatId));

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
               🔗 Успішно!
               
               Пристрій "%s" привʼязано до каналу %d.
               """.formatted(device.getName(), chatId);
    }

    // =========================
    // UNBIND
    // =========================

    @Transactional
    public String unbind(User user, Long chatId, String deviceKey) {
        Device device = deviceService.findByKeyOrThrow(deviceKey);

        Channel channel = channelRepo.findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Канал не знайдено."));

        Subscription sub = subscriptionRepo
                .findByDeviceAndChannel(device, channel)
                .orElseThrow(() -> new IllegalArgumentException("Привʼязку не знайдено."));

        subscriptionRepo.delete(sub);

        return "🔓 Пристрій відвʼязано від каналу " + chatId;
    }

    // =========================
    // HELPER METHODS
    // =========================

    private Channel createNewChannel(User user, Long chatId) {
        Channel ch = new Channel();
        ch.setChatId(chatId);
        ch.setUser(user);
        ch.setType(ChannelType.CHANNEL);
        ch.setCreatedAt(Instant.now());
        return channelRepo.save(ch);
    }

    private String formatDuration(Instant lastSeen) {
        long seconds = Duration.between(lastSeen, Instant.now()).getSeconds();

        if (seconds < 60) {
            return seconds + " сек тому";
        } else if (seconds < 3600) {
            return (seconds / 60) + " хв тому";
        } else if (seconds < 86400) {
            return (seconds / 3600) + " год тому";
        } else {
            return (seconds / 86400) + " дн тому";
        }
    }
}

