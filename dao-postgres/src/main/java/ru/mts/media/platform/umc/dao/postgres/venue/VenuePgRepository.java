package ru.mts.media.platform.umc.dao.postgres.venue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.mts.media.platform.umc.dao.postgres.common.FullExternalIdPk;

import java.util.List;

@Repository
public interface VenuePgRepository extends JpaRepository<VenuePgEntity, FullExternalIdPk> {

    VenuePgEntity findByReferenceId(String referenceId);

    @Query("""
        SELECT DISTINCT v FROM VenuePgEntity v
        JOIN v.events e
        WHERE e.id = :eventId
        """)
    List<VenuePgEntity> findVenuesByEventId(@Param("eventId") Long eventId);
}
