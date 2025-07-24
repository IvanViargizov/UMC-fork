package ru.mts.media.platform.umc.dao.postgres.event;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.mts.media.platform.umc.domain.gql.types.Event;

import java.time.LocalDateTime;

import static org.mapstruct.MappingConstants.ComponentModel.SPRING;

@Mapper(componentModel = SPRING)
public interface EventPgMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "localDateTimeToString")
    @Mapping(target = "venues", ignore = true) // Заполняется через DataFetcher
    Event asModel(EventPgEntity entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "startTime", source = "startTime", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "endTime", source = "endTime", qualifiedByName = "stringToLocalDateTime")
    @Mapping(target = "venues", ignore = true) // Игнорируем при создании Entity
    EventPgEntity asEntity(Event event);

    @Named("localDateTimeToString")
    default String localDateTimeToString(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.toString() : null;
    }

    @Named("stringToLocalDateTime")
    default LocalDateTime stringToLocalDateTime(String dateTime) {
        return dateTime != null ? LocalDateTime.parse(dateTime) : null;
    }
}