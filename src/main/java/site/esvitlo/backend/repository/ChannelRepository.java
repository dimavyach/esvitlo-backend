package site.esvitlo.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import site.esvitlo.backend.domain.Channel;

import java.util.Optional;

public interface ChannelRepository extends JpaRepository<Channel, Long> {

    Optional<Object> findByChatId(Long chatId);
}
