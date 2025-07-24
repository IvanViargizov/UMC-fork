package ru.mts.media.platform.umc.api.gql.event;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgEntity;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgMapper;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgRepository;
import ru.mts.media.platform.umc.dao.postgres.venue.VenuePgMapper;
import ru.mts.media.platform.umc.domain.gql.types.CreateEventInput;
import ru.mts.media.platform.umc.domain.gql.types.Event;
import ru.mts.media.platform.umc.domain.venue.VenueSot;

import java.time.LocalDateTime;

@DgsComponent
@RequiredArgsConstructor
public class EventDgsMutation {
    private final VenueSot venueSot;
    private final VenuePgMapper venuePgMapper;

    private final EventPgRepository eventRepository;
    private final EventPgMapper eventPgMapper;

    @DgsMutation
    @Transactional
    public Event createEvent(@InputArgument CreateEventInput input) {
        EventPgEntity event = new EventPgEntity();
        event.setName(input.getName());
        event.setStartTime(LocalDateTime.parse(input.getStartTime()));
        event.setEndTime(LocalDateTime.parse(input.getEndTime()));

        if (input.getVenueReferenceIds() != null) {
            for (String venueReferenceId : input.getVenueReferenceIds()) {
                venueSot.getVenueByReferenceId(venueReferenceId)
                        .map(venuePgMapper::asEntity)
                        .ifPresent(event.getVenues()::add);
            }
        }

        EventPgEntity savedEvent = eventRepository.save(event);
        return eventPgMapper.asModel(savedEvent);
    }
}
