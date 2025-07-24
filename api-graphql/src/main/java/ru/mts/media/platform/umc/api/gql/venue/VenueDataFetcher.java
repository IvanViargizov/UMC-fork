package ru.mts.media.platform.umc.api.gql.venue;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgMapper;
import ru.mts.media.platform.umc.dao.postgres.event.EventPgRepository;
import ru.mts.media.platform.umc.domain.gql.types.Event;
import ru.mts.media.platform.umc.domain.gql.types.Venue;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class VenueDataFetcher {
    
    private final EventPgRepository eventRepository;
    private final EventPgMapper eventMapper;

    @DgsData(parentType = "Venue", field = "latestEvents")
    public List<Event> latestEvents(DgsDataFetchingEnvironment dfe) {
        Venue venue = dfe.getSource();
        String venueReferenceId = venue.getId();
        
        return eventRepository.findLatestEventsByVenueReferenceId(venueReferenceId)
                .stream()
                .map(eventMapper::asModel)
                .collect(Collectors.toList());
    }
}