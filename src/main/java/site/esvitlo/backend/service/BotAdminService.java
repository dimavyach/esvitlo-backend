package site.esvitlo.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import site.esvitlo.backend.domain.Channel;
import site.esvitlo.backend.domain.ChannelType;
import site.esvitlo.backend.domain.Device;
import site.esvitlo.backend.domain.Subscription;
import site.esvitlo.backend.repository.ChannelRepository;
import site.esvitlo.backend.repository.DeviceRepository;
import site.esvitlo.backend.repository.SubscriptionRepository;

@Service
public class BotAdminService {

    private final ChannelRepository channelRepo;
    private final DeviceRepository deviceRepo;
    private final SubscriptionRepository subRepo;

    public BotAdminService(ChannelRepository channelRepo,
                           DeviceRepository deviceRepo,
                           SubscriptionRepository subRepo) {
        this.channelRepo = channelRepo;
        this.deviceRepo = deviceRepo;
        this.subRepo = subRepo;
    }

    @Transactional
    public String addChannel(Long chatId) {
        channelRepo.findByChatId(chatId)
                .orElseGet(() -> channelRepo.save(
                        new Channel(chatId, ChannelType.CHANNEL)
                ));
        return "Канал додано: " + chatId;
    }

    @Transactional
    public String bind(Long chatId, String deviceKey) {

        Channel channel = (Channel) channelRepo.findByChatId(chatId)
                .orElseThrow(() -> new IllegalArgumentException("Канал не знайдено. Спочатку add_channel."));

        Device device = deviceRepo.findByDeviceKey(deviceKey)
                .orElseThrow(() -> new IllegalArgumentException("Девайс не знайдено. Перевір device_key."));

        subRepo.save(new Subscription(device, channel));

        return "Привʼязано: " + device.getName() + " → канал " + chatId;
    }
}

