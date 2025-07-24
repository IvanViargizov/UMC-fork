package ru.mts.media.platform.umc.api.gql.event;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsData;
import com.netflix.graphql.dgs.DgsDataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import ru.mts.media.platform.umc.dao.postgres.venue.VenuePgMapper;
import ru.mts.media.platform.umc.dao.postgres.venue.VenuePgRepository;
import ru.mts.media.platform.umc.domain.gql.types.Event;
import ru.mts.media.platform.umc.domain.gql.types.Venue;

import java.util.List;
import java.util.stream.Collectors;

@DgsComponent
@RequiredArgsConstructor
public class EventDataFetcher {
    
    private final VenuePgRepository venueRepository;
    private final VenuePgMapper venueMapper;

    @DgsData(parentType = "Event", field = "venues")
    public List<Venue> venues(DgsDataFetchingEnvironment dfe) {
        Event event = dfe.getSource();
        Long eventId = Long.valueOf(event.getId());
        
        return venueRepository.findVenuesByEventId(eventId)
                .stream()
                .map(venueMapper::asModel)
                .collect(Collectors.toList());
    }
}