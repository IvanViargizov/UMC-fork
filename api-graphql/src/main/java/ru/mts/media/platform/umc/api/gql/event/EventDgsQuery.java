package ru.mts.media.platform.umc.api.gql.event;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsQuery;
import lombok.RequiredArgsConstructor;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgMapper;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgRepository;
import ru.mts.media.platform.umc.domain.gql.types.Event;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class EventDgsQuery {
    private final EventPgRepository eventRepository;
    private final EventPgMapper eventMapper;

    @DgsQuery
    public List<Event> events() {
        return eventRepository.findAll().stream()
                .map(eventMapper::asModel)
                .collect(Collectors.toList());
    }
}
