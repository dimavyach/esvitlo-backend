package site.esvitlo.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name="devices")

public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String deviceKey;

    @Getter
    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Instant lastSeen;

    @Column(nullable = false)
    private boolean online;

    protected Device() {
    }

    public Device(String deviceKey, String name) {
        this.deviceKey = deviceKey;
        this.name = name;
        this.lastSeen = Instant.now();
        this.online = true;
    }

    public void markSeen() {
        this.lastSeen = Instant.now();
        this.online = true;
    }

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

}

