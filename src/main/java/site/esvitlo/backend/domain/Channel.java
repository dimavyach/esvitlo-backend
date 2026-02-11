package site.esvitlo.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
@Getter
@Setter
@Entity
@Table(name = "channels")
public class Channel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chat_id", nullable = false, unique = true)
    private Long chatId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChannelType type;

    @Column(nullable = false)
    private Instant createdAt;

    protected Channel() {
    }

    public Channel(Long chatId, ChannelType type) {
        this.chatId = chatId;
        this.type = type;
        this.createdAt = Instant.now();
    }
}


