CREATE TABLE venue_event
(
    venue_brand       VARCHAR(255) NOT NULL,
    venue_provider    VARCHAR(255) NOT NULL,
    venue_external_id VARCHAR(255) NOT NULL,
    event_id          BIGINT       NOT NULL,

    PRIMARY KEY (venue_brand, venue_provider, venue_external_id, event_id),

    CONSTRAINT fk_venue_event_venue
        FOREIGN KEY (venue_brand, venue_provider, venue_external_id)
            REFERENCES venue (brand, provider, external_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_venue_event_event
        FOREIGN KEY (event_id)
            REFERENCES event (id)
            ON DELETE CASCADE
);