## Ссылка на исходный репозиторий с тестовым заданием и README с задачами тестового задания
- [Исходный репозиторий с тестовым заданием](https://github.com/Lildagi/UMC)

## Комментарии к решению

### 1. Архитектура работы с Event

Для ускорения выполнения задания для сущности Event использован прямой доступ к `EventPgRepository`, в отличие от абстракции `VenueSot` для Venue

**Правильная архитектура должна включать:**
- `EventSot` интерфейс для абстракции доступа к данным
- `EventDomainService` для бизнес-логики
- `EventSave` событие для координации между слоями
- Реализацию `EventSot` в dao-слое с обработкой доменных событий

### 2. Работа с Netflix DGS Framework

**Реализованные компоненты:**
- **Query**: `EventDgsQuery` для получения списка событий
- **Mutation**: `EventDgsMutation` для создания новых событий

### 3. Решение проблемы N+1 запросов

**Решение через JOIN FETCH:**
- `EventPgRepository.findAllWithVenues()` - использует `LEFT JOIN FETCH e.venues` для загрузки событий со всеми местами проведения за один запрос
- `EventPgRepository.findByIdWithVenues()` - аналогично для одного события
- `VenuePgRepository.findAllWithEvents()` - использует `LEFT JOIN FETCH v.events` для загрузки мест проведения со всеми событиями
- `VenuePgRepository.findByReferenceIdWithEvents()` - для одного места проведения

### 4. Функциональное и нагрузочное тестирование

**ApplicationFunctionalTest:**
- Использует Testcontainers с PostgreSQL для изоляции тестов
- Проверяет полный цикл: создание venues → создание event → проверка связей
- Тестирует GraphQL запросы и мутации end-to-end
- Проверяет работу DataFetcher через запросы с вложенными полями

**ApplicationPerformanceTest:**
- Измеряет RPS и выявляет узкие места в производительности

### 5. Ограничения реализации Many-to-Many

Хотя связь Event-Venue реализована как Many-to-Many на уровне БД и JPA, в GraphQL API не добавлена возможность создавать Venue с привязкой к существующим Event. Согласно ТЗ требовалось только:
- Создание Event с указанием venue-referenceId
- Получение списка событий с площадками
- Получение мест проведения с последними событиями

### 6. Добавленные зависимости

- `spring-boot-starter-data-jpa` - для работы с JPA сущностями в GraphQL слое, необходимо для использования EventPgRepository напрямую в EventDgsQuery и EventDgsMutation
- `graphql-dgs-spring-graphql-starter` - основной DGS фреймворк для создания GraphQL API (@DgsComponent, @DgsQuery, @DgsMutation аннотации)
- `spring-boot-starter-test` - базовые тестовые зависимости Spring Boot для ApplicationFunctionalTest и ApplicationPerformanceTest
- `graphql-dgs-spring-graphql-starter-test` - специальные тестовые утилиты DGS, в частности DgsQueryExecutor для выполнения GraphQL запросов в тестах
- `org.testcontainers:junit-jupiter` - интеграция Testcontainers с JUnit 5 для @Testcontainers и @Container аннотаций в тестах
- `org.testcontainers:postgresql` - PostgreSQL контейнер для изоляции функциональных и нагрузочных тестов от внешней БД

### 7. Предложения по улучшению

- Добавить валидацию бизнес-правил (например, startTime < endTime и пр.)
- Добавить обработку случая, когда venue не найден по referenceId
- Добавить централизованную обработку исключений

**Дальнейшие улучшения по моему представленному решению:**
- Создать `EventDomainService` и `EventSot` по аналогии с Venue
- Хардкод значения 10 в запросе последних событий - нужно сделать конфигурируемым
- Отсутствие кеширования
- Отсутствие пагинации


