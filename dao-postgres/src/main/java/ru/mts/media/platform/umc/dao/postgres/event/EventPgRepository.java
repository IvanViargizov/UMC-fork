package ru.mts.media.platform.umc.dao.postgres.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventPgRepository extends JpaRepository<EventPgEntity, Long> {

    @Query(value = """
            SELECT DISTINCT e.* FROM event e
            JOIN venue_event ve ON e.id = ve.event_id
            JOIN venue v ON ve.venue_brand = v.brand 
                         AND ve.venue_provider = v.provider 
                         AND ve.venue_external_id = v.external_id
            WHERE v.reference_id = :venueReferenceId
            ORDER BY e.start_time DESC
            LIMIT 10
            """, nativeQuery = true)
    List<EventPgEntity> findLatestEventsByVenueReferenceId(@Param("venueReferenceId") String venueReferenceId);
}
