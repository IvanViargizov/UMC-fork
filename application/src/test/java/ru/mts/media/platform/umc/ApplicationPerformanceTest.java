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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(properties = {
        "spring.config.name=application,kafka,postgres,graphql"
})
public class ApplicationPerformanceTest {

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
    void shouldHandleMultipleVenueCreationRequests() {
        // Параметры
        int numberOfRequests = 50; // Количество запросов
        int concurrentUsers = 10;   // Одновременных пользователей

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        CompletableFuture<Void>[] tasks = new CompletableFuture[numberOfRequests];

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;

            tasks[i] = CompletableFuture.runAsync(() -> {
                try {
                    @Language("GraphQL") String createVenueMutation = """
                            mutation CreateVenue($id: FullExternalIdInput!, $input: SaveVenueInput!) {
                                saveVenue(id: $id, input: $input) {
                                    id
                                    name
                                }
                            }
                            """;

                    Map<String, Object> variables = Map.of(
                            "id", Map.of(
                                    "brandId", "load-test-brand-" + requestId,
                                    "providerId", "load-test-provider-" + requestId,
                                    "externalId", "venue-" + requestId
                            ),
                            "input", Map.of("name", "Нагрузочная площадка " + requestId)
                    );

                    String venueId = dgsQueryExecutor.executeAndExtractJsonPath(
                            createVenueMutation,
                            "data.saveVenue.id",
                            variables
                    );

                    if (venueId != null) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Ошибка в запросе " + requestId + ": " + e.getMessage());
                }
            }, executor);
        }

        CompletableFuture.allOf(tasks).join();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();

        System.out.println("=== РЕЗУЛЬТАТЫ НАГРУЗОЧНОГО ТЕСТА VENUE ===");
        System.out.println("Общее время выполнения: " + duration + " мс");
        System.out.println("Успешных запросов: " + successCount.get());
        System.out.println("Ошибок: " + errorCount.get());
        System.out.println("RPS (запросов в секунду): " + (numberOfRequests * 1000.0 / duration));
        System.out.println("Среднее время на запрос: " + (duration / (double) numberOfRequests) + " мс");

        assertThat(successCount.get()).isEqualTo(numberOfRequests);
        assertThat(errorCount.get()).isEqualTo(0);
    }

    @Test
    void shouldHandleMultipleEventCreationRequests() {
        // Параметры
        int numberOfRequests = 30;
        int concurrentUsers = 5;

        String venueId1 = createTestVenue("load-event-venue-1");
        String venueId2 = createTestVenue("load-event-venue-2");

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        CompletableFuture<Void>[] tasks = new CompletableFuture[numberOfRequests];

        for (int i = 0; i < numberOfRequests; i++) {
            final int requestId = i;

            tasks[i] = CompletableFuture.runAsync(() -> {
                try {
                    @Language("GraphQL") String createEventMutation = """
                            mutation CreateEvent($input: CreateEventInput!) {
                                createEvent(input: $input) {
                                    id
                                    name
                                    venues {
                                        id
                                        name
                                    }
                                }
                            }
                            """;

                    Map<String, Object> variables = Map.of(
                            "input", Map.of(
                                    "venueReferenceIds", List.of(venueId1, venueId2),
                                    "name", "Нагрузочное событие " + requestId,
                                    "startTime", "2024-12-20T" + String.format("%02d", (requestId % 24)) + ":00:00",
                                    "endTime", "2024-12-20T" + String.format("%02d", ((requestId % 24) + 1) % 24) + ":00:00"
                            )
                    );

                    String eventName = dgsQueryExecutor.executeAndExtractJsonPath(
                            createEventMutation,
                            "data.createEvent.name",
                            variables
                    );

                    if (eventName != null && eventName.contains("Нагрузочное событие")) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Ошибка в создании события " + requestId + ": " + e.getMessage());
                }
            }, executor);
        }

        CompletableFuture.allOf(tasks).join();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();

        System.out.println("=== РЕЗУЛЬТАТЫ НАГРУЗОЧНОГО ТЕСТА СОБЫТИЙ ===");
        System.out.println("Общее время выполнения: " + duration + " мс");
        System.out.println("Успешных запросов: " + successCount.get());
        System.out.println("Ошибок: " + errorCount.get());
        System.out.println("RPS: " + (numberOfRequests * 1000.0 / duration));

        assertThat(successCount.get()).isEqualTo(numberOfRequests);
        assertThat(errorCount.get()).isEqualTo(0);
    }

    @Test
    void shouldHandleMultipleReadRequests() {
        // Параметры нагрузочного теста
        int numberOfRequests = 100;
        int concurrentUsers = 20;

        // Создаем тестовые данные
        String venueId1 = createTestVenue("read-test-venue-1");
        String venueId2 = createTestVenue("read-test-venue-2");

        // Создаем тестовое событие
        createTestEvent(venueId1, venueId2, "test-read-event");

        ExecutorService executor = Executors.newFixedThreadPool(concurrentUsers);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        long startTime = System.currentTimeMillis();

        CompletableFuture<Void>[] tasks = new CompletableFuture[numberOfRequests];

        for (int i = 0; i < numberOfRequests; i++) {
            tasks[i] = CompletableFuture.runAsync(() -> {
                try {
                    @Language("GraphQL") String query = """
                            query GetAllData {
                                venues {
                                    id
                                    name
                                    latestEvents {
                                        id
                                        name
                                    }
                                }
                                events {
                                    id
                                    name
                                    venues {
                                        id
                                        name
                                    }
                                }
                            }
                            """;

                    Integer venuesCount = dgsQueryExecutor.executeAndExtractJsonPath(
                            query,
                            "data.venues.length()"
                    );

                    Integer eventsCount = dgsQueryExecutor.executeAndExtractJsonPath(
                            query,
                            "data.events.length()"
                    );

                    if (venuesCount != null && eventsCount != null &&
                            venuesCount > 0 && eventsCount > 0) {
                        successCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }

                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    System.err.println("Ошибка в чтении данных: " + e.getMessage());
                }
            }, executor);
        }

        CompletableFuture.allOf(tasks).join();

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        executor.shutdown();

        System.out.println("=== РЕЗУЛЬТАТЫ НАГРУЗОЧНОГО ТЕСТА ЧТЕНИЯ ===");
        System.out.println("Общее время выполнения: " + duration + " мс");
        System.out.println("Успешных запросов: " + successCount.get());
        System.out.println("Ошибок: " + errorCount.get());
        System.out.println("RPS: " + (numberOfRequests * 1000.0 / duration));

        assertThat(successCount.get()).isEqualTo(numberOfRequests);
        assertThat(errorCount.get()).isEqualTo(0);
    }

    private String createTestVenue(String suffix) {
        @Language("GraphQL") String createVenueMutation = """
                mutation CreateVenue($id: FullExternalIdInput!, $input: SaveVenueInput!) {
                    saveVenue(id: $id, input: $input) {
                        id
                        name
                    }
                }
                """;

        Map<String, Object> variables = Map.of(
                "id", Map.of(
                        "brandId", "test-brand-" + suffix,
                        "providerId", "test-provider-" + suffix,
                        "externalId", suffix
                ),
                "input", Map.of("name", "Тестовая площадка " + suffix)
        );

        return dgsQueryExecutor.executeAndExtractJsonPath(
                createVenueMutation,
                "data.saveVenue.id",
                variables
        );
    }

    private void createTestEvent(String venueId1, String venueId2, String eventName) {
        @Language("GraphQL") String createEventMutation = """
                mutation CreateEvent($input: CreateEventInput!) {
                    createEvent(input: $input) {
                        id
                        name
                    }
                }
                """;

        Map<String, Object> variables = Map.of(
                "input", Map.of(
                        "venueReferenceIds", List.of(venueId1, venueId2),
                        "name", eventName,
                        "startTime", "2024-12-20T10:00:00",
                        "endTime", "2024-12-20T12:00:00"
                )
        );

        dgsQueryExecutor.executeAndExtractJsonPath(
                createEventMutation,
                "data.createEvent.id",
                variables
        );
    }
}