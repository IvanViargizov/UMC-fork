package ru.mts.media.platform.umc.dao.postgres.event;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import ru.mts.media.platform.umc.dao.postgres.venue.VenuePgEntity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@Table(name = "event")
public class EventPgEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToMany(
            fetch   = FetchType.LAZY,
            cascade = { CascadeType.DETACH, CascadeType.REFRESH }
    )
    @JoinTable(
            name = "venue_event",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = {
                    @JoinColumn(name = "venue_brand", referencedColumnName = "brand"),
                    @JoinColumn(name = "venue_provider", referencedColumnName = "provider"),
                    @JoinColumn(name = "venue_external_id", referencedColumnName = "externalId")
            }
    )
    private Set<VenuePgEntity> venues = new HashSet<>();
}
