package site.esvitlo.backend.repository;

import site.esvitlo.backend.domain.Device;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface DeviceRepository extends JpaRepository<Device, Long>
{
    Optional<Device> findByDeviceKey(String deviceKey);

    List<Device> findByOnlineTrueAndLastSeenBefore(Instant cutoff);

    List<Device> findByUserId(Long userId);
}
