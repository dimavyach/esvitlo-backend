package site.esvitlo.backend.service;

import org.springframework.stereotype.Service;
import site.esvitlo.backend.domain.Device;
import site.esvitlo.backend.domain.Subscription;
import site.esvitlo.backend.repository.SubscriptionRepository;
import site.esvitlo.backend.telegram.TelegramClient;

import java.util.List;

@Service
public class NotificationService {

    private final SubscriptionRepository subscriptionRepository;
    private final TelegramClient telegramClient;

    public NotificationService(SubscriptionRepository subscriptionRepository,
                               TelegramClient telegramClient) {
        this.subscriptionRepository = subscriptionRepository;
        this.telegramClient = telegramClient;
    }

    public void deviceOnline(Device device) {
        notify(device, "⚡ Світло зʼявилось\n📱 " + device.getName());
    }

    public void deviceOffline(Device device) {
        notify(device, "❌ Світло зникло\n📱 " + device.getName());
    }

    private void notify(Device device, String text) {
        List<Subscription> subs =
                subscriptionRepository.findByDevice_Id(device.getId());

        for (Subscription sub : subs) {
            telegramClient.sendMessage(
                    sub.getChannel().getChatId(),
                    text
            );
        }
    }
}

