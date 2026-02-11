package site.esvitlo.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@DependsOn("entityManagerFactory")
public class DeviceStatusScheduler {

    private final DeviceService deviceService;
    private final Duration timeout;

    public DeviceStatusScheduler(
            DeviceService deviceService,
            @Value("${device.offline-timeout-seconds}") long seconds
    ) {
        this.deviceService = deviceService;
        this.timeout = Duration.ofSeconds(seconds);
    }

    @Scheduled(fixedDelay = 30000)
    public void checkOfflineDevices() {
        deviceService.markOfflineIfExpired(timeout);
    }
}
