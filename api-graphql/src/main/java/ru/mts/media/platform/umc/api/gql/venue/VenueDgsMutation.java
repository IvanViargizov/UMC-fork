package ru.mts.media.platform.umc.api.gql.venue;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsMutation;
import com.netflix.graphql.dgs.InputArgument;
import lombok.RequiredArgsConstructor;
import ru.mts.media.platform.umc.domain.gql.types.FullExternalId;
import ru.mts.media.platform.umc.domain.gql.types.SaveVenueInput;
import ru.mts.media.platform.umc.domain.gql.types.Venue;
import ru.mts.media.platform.umc.domain.venue.VenueDomainService;

@DgsComponent
@RequiredArgsConstructor
public class VenueDgsMutation {
    private final VenueDomainService domainService;

    @DgsMutation
    public Venue saveVenue(@InputArgument FullExternalId id,
                           @InputArgument SaveVenueInput input) {
        return domainService.save(id, input).getEntity();
    }
}
