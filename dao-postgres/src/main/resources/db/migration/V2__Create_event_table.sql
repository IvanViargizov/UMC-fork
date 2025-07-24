CREATE TABLE event
(
    id         BIGSERIAL    NOT NULL,
    name       VARCHAR(255) NOT NULL,
    start_time TIMESTAMP    NOT NULL,
    end_time   TIMESTAMP    NOT NULL,
    PRIMARY KEY (id)
);