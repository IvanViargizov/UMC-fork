package ru.mts.media.platform.umc.dao.postgres.venue;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgMapper;
import ru.mts.media.platform.umc.domain.gql.types.FullExternalId;
import ru.mts.media.platform.umc.domain.gql.types.Venue;
import ru.mts.media.platform.umc.domain.venue.VenueSave;
import ru.mts.media.platform.umc.domain.venue.VenueSot;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
class VenuePgDao implements VenueSot {
    private final VenuePgRepository repository;
    private final VenuePgMapper mapper;
    private final EventPgMapper eventMapper;

    @Override
    public Optional<Venue> getVenueByReferenceId(String id) {
        return Optional.of(id)
                .map(repository::findByReferenceIdWithEvents)
                .map(entity -> {
                    Venue venue = mapper.asModel(entity);
                    // Загружаем последние 10 events
                    venue.setLatestEvents(entity.getEvents().stream()
                            .sorted(Comparator.comparing(e -> e.getStartTime(), Comparator.reverseOrder()))
                            .limit(10)
                            .map(eventMapper::asModel)
                            .collect(Collectors.toList()));
                    return venue;
                });
    }

    @Override
    public Optional<Venue> getVenueById(FullExternalId externalId) {
        return Optional.of(externalId)
                .map(mapper::asPk)
                .flatMap(repository::findById)
                .map(mapper::asModel);
    }

    @Override
    public List<Venue> getAllVenues() {
        return repository.findAllWithEvents().stream()
                .map(entity -> {
                    Venue venue = mapper.asModel(entity);
                    // Events уже загружены, берем последние 10
                    venue.setLatestEvents(entity.getEvents().stream()
                            .sorted(Comparator.comparing(e -> e.getStartTime(), Comparator.reverseOrder()))
                            .limit(10)
                            .map(eventMapper::asModel)
                            .collect(Collectors.toList()));
                    return venue;
                })
                .collect(Collectors.toList());
    }

    @EventListener
    public void handleVenueCreatedEvent(VenueSave evt) {
        evt.unwrap()
                .map(mapper::asEntity)
                .ifPresent(repository::save);
    }
}
