package site.esvitlo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.esvitlo.backend.domain.Channel;
import site.esvitlo.backend.domain.Device;
import site.esvitlo.backend.domain.Subscription;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    //List<Subscription> findByDeviceId(Long deviceId);

    List<Subscription> findByDevice_Id(Long deviceId);
    List<Subscription> findByChannel_Id(Long channelId);

    Optional<Subscription> findByDeviceAndChannel(Device device, Channel channel);
}
