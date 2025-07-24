package ru.mts.media.platform.umc.dao.postgres.venue;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.mts.media.platform.umc.dao.postgres.common.FullExternalIdPk;
import ru.mts.media.platform.umc.domain.gql.types.FullExternalId;
import ru.mts.media.platform.umc.domain.gql.types.Venue;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface VenuePgMapper {
    @Mapping(target = "externalId.brandId", source = "brand")
    @Mapping(target = "externalId.providerId", source = "provider")
    @Mapping(target = "externalId.externalId", source = "externalId")
    @Mapping(target = "id", source = "referenceId")
    @Mapping(target = "latestEvents", ignore = true) // Заполняется через DataFetcher
    Venue asModel(VenuePgEntity venuePg);

    @Mapping(target = "referenceId", source = "id")
    @Mapping(target = "brand", source = "externalId.brandId")
    @Mapping(target = "provider", source = "externalId.providerId")
    @Mapping(target = "externalId", source = "externalId.externalId")
    @Mapping(target = "events", ignore = true) // Заполняется через DataFetcher
    VenuePgEntity asEntity(Venue venue);

    @Mapping(target = "brand", source = "brandId")
    @Mapping(target = "provider", source = "providerId")
    @Mapping(target = "externalId", source = "externalId")
    FullExternalIdPk asPk(FullExternalId fullExternalId);
}
