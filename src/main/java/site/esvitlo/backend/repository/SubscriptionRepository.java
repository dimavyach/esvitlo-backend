package site.esvitlo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.esvitlo.backend.domain.Subscription;

import java.util.List;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByDeviceId(Long deviceId);

    List<Subscription> findByChannelId(Long channelId);
}
