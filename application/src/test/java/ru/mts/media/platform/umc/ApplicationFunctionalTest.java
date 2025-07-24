package ru.mts.media.platform.umc;

import com.netflix.graphql.dgs.DgsQueryExecutor;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "spring.config.name=application,kafka,postgres,graphql"
})
public class ApplicationFunctionalTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("umc_test")
                    .withUsername("postgres-test")
                    .withPassword("postgres-test");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "true");
    }

    @Autowired
    private DgsQueryExecutor dgsQueryExecutor;

    @Test
    void createVenueAndEventThroughGraphql() {
        // Создаем первую venue
        @Language("GraphQL") String createVenueMutation1 = """
                mutation CreateVenue1($id: FullExternalIdInput!, $input: SaveVenueInput!) {
                    saveVenue(id: $id, input: $input) {
                        id
                        name
                        externalId {
                            brandId
                            providerId
                            externalId
                        }
                    }
                }
                """;

        Map<String, Object> venueVariables1 = Map.of(
                "id", Map.of(
                        "brandId", "brand 1",
                        "providerId", "provider 1",
                        "externalId", "venue 1"
                ),
                "input", Map.of("name", "Площадка 1")
        );

        String venueId1 = dgsQueryExecutor.executeAndExtractJsonPath(
                createVenueMutation1,
                "data.saveVenue.id",
                venueVariables1
        );

        assertThat(venueId1).isNotNull();

        // Создаем вторую venue
        @Language("GraphQL") String createVenueMutation2 = """
                mutation CreateVenue2($id: FullExternalIdInput!, $input: SaveVenueInput!) {
                    saveVenue(id: $id, input: $input) {
                        id
                        name
                        externalId {
                            brandId
                            providerId
                            externalId
                        }
                    }
                }
                """;

        Map<String, Object> venueVariables2 = Map.of(
                "id", Map.of(
                        "brandId", "brand 2",
                        "providerId", "provider 2",
                        "externalId", "venue 2"
                ),
                "input", Map.of("name", "Площадка 2")
        );

        String venueId2 = dgsQueryExecutor.executeAndExtractJsonPath(
                createVenueMutation2,
                "data.saveVenue.id",
                venueVariables2
        );

        assertThat(venueId2).isNotNull();

        @Language("GraphQL") String createEventMutation = """
                mutation CreateEvent($input: CreateEventInput!) {
                    createEvent(input: $input) {
                        id
                        name
                        startTime
                        endTime
                        venues {
                            id
                            name
                        }
                    }
                }
                """;

        Map<String, Object> eventVariables = Map.of(
                "input", Map.of(
                        "venueReferenceIds", List.of(venueId1, venueId2),
                        "name", "Event 1",
                        "startTime", "2025-07-24T12:00:00",
                        "endTime", "2025-12-31T22:00:00"
                )
        );

        String eventName = dgsQueryExecutor.executeAndExtractJsonPath(
                createEventMutation,
                "data.createEvent.name",
                eventVariables
        );

        assertThat(eventName).isEqualTo("Event 1");

        // Проверяем, что event связан с несколькими venue
        Integer venuesCount = dgsQueryExecutor.executeAndExtractJsonPath(
                createEventMutation,
                "data.createEvent.venues.length()",
                eventVariables
        );

        assertThat(venuesCount).isEqualTo(2);

        // Проверяем получение списка events
        @Language("GraphQL") String eventsQuery = """
                query GetEvents {
                    events {
                        id
                        name
                        startTime
                        endTime
                        venues {
                            id
                            name
                        }
                    }
                }
                """;

        Integer eventsCount = dgsQueryExecutor.executeAndExtractJsonPath(
                eventsQuery,
                "data.events.length()"
        );

        assertThat(eventsCount).isGreaterThan(0);

        // Проверяем получение списка venues с latestEvents
        @Language("GraphQL") String venuesQuery = """
                query GetVenues {
                    venues {
                        id
                        name
                        latestEvents {
                            id
                            name
                        }
                        externalId {
                            brandId
                            providerId
                            externalId
                        }
                    }
                }
                """;

        Integer venuesCountResult = dgsQueryExecutor.executeAndExtractJsonPath(
                venuesQuery,
                "data.venues.length()"
        );

        assertThat(venuesCountResult).isEqualTo(2);

        String firstVenueName = dgsQueryExecutor.executeAndExtractJsonPath(
                venuesQuery,
                "data.venues[0].name"
        );

        assertThat(firstVenueName).isNotNull();
    }
}