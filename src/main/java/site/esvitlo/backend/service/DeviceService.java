package site.esvitlo.backend.service;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.h2console.autoconfigure.H2ConsoleAutoConfiguration;
import org.springframework.transaction.annotation.Transactional;
import site.esvitlo.backend.domain.Device;
import site.esvitlo.backend.repository.DeviceRepository;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter

@Service
public class DeviceService {

    private final DeviceRepository repository;
    private final NotificationService notificationService;


    public DeviceService(DeviceRepository repository,
                         NotificationService notificationService) {
        this.repository = repository;
        this.notificationService = notificationService;
    }

    public Device findByKeyOrThrow(String deviceKey) {
        return repository.findByDeviceKey(deviceKey)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid device key: " + deviceKey)
                );
    }

    @Transactional
    public void registerPing(Device device) {

        boolean wasOffline = !device.isOnline();

        device.markSeen();
        repository.save(device);

        if (wasOffline) {
            notificationService.deviceOnline(device);
            log.info("Device ONLINE: {}", device.getName());
        }
    }

    @Transactional
    public void markOfflineIfExpired(Duration timeout) {
        Instant cutoff = Instant.now().minus(timeout);

        List<Device> expired =
                repository.findByOnlineTrueAndLastSeenBefore(cutoff);

        for (Device device : expired) {

            if (device.isOnline()) { // 🔴 ТІЛЬКИ ЯКЩО БУВ ONLINE
                device.setOnline(false);
                repository.save(device);

                notificationService.deviceOffline(device);
                log.info("Device OFFLINE: {}", device.getName());
            }
        }
    }
    public Device createDevice(String name) {
        String key = UUID.randomUUID().toString();

        Device device = new Device(key, name);
        return repository.save(device);
    }
}

