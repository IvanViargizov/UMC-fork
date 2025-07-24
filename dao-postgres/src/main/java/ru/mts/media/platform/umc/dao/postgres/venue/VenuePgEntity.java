package ru.mts.media.platform.umc.dao.postgres.venue;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import ru.mts.media.platform.umc.dao.postgres.common.FullExternalIdPk;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgEntity;

import java.util.HashSet;
import java.util.Set;

@Entity
@Data
@IdClass(FullExternalIdPk.class)
@Table(name = "venue",
        indexes = {
                @Index(name = "idx_venue_referenceId",
                        columnList = "referenceId",
                        unique = true)
        })
public class VenuePgEntity {
    @Id
    private String brand;

    @Id
    private String provider;

    @Id
    private String externalId;

    private String referenceId;

    private String name;

    @ManyToMany(mappedBy = "venues", fetch = FetchType.LAZY)
    private Set<EventPgEntity> events = new HashSet<>();
}
