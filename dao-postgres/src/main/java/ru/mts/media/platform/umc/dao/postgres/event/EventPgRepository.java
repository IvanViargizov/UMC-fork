package ru.mts.media.platform.umc.dao.postgres.event;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventPgRepository extends JpaRepository<EventPgEntity, Long> {

    @Query("SELECT DISTINCT e FROM EventPgEntity e LEFT JOIN FETCH e.venues")
    List<EventPgEntity> findAllWithVenues();

    @Query("SELECT e FROM EventPgEntity e LEFT JOIN FETCH e.venues WHERE e.id = :id")
    Optional<EventPgEntity> findByIdWithVenues(@Param("id") Long id);
}
