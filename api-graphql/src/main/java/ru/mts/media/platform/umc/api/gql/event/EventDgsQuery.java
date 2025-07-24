package ru.mts.media.platform.umc.api.gql.event;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import lombok.RequiredArgsConstructor;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgMapper;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgRepository;
import ru.mts.media.platform.umc.dao.postgres.venue.VenuePgMapper;
import ru.mts.media.platform.umc.domain.gql.types.Event;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class EventDgsQuery {
    private final EventPgRepository eventRepository;
    private final EventPgMapper eventMapper;
    private final VenuePgMapper venueMapper;

    @DgsQuery
    public List<Event> events() {
        return eventRepository.findAllWithVenues().stream()
                .map(entity -> {
                    Event event = eventMapper.asModel(entity);
                    event.setVenues(entity.getVenues().stream()
                            .map(venueMapper::asModel)
                            .collect(Collectors.toList()));
                    return event;
                })
                .collect(Collectors.toList());
    }
}