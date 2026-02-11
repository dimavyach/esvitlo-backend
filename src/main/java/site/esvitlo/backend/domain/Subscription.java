package site.esvitlo.backend.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

@Entity
@Table(
        name = "subscriptions",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"device_id", "channel_id"})
        }
)
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "device_id")
    private Device device;

    @ManyToOne(optional = false)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    public Subscription() {
    }

    public Subscription(Device device, Channel channel) {
        this.device = device;
        this.channel = channel;
    }

}
