CREATE TABLE venue
(
    brand        VARCHAR(255) NOT NULL,
    provider     VARCHAR(255) NOT NULL,
    external_id  VARCHAR(255) NOT NULL,
    reference_id VARCHAR(255) NOT NULL,
    name         VARCHAR(255) NOT NULL,
    PRIMARY KEY (brand, provider, external_id)
);

CREATE UNIQUE INDEX idx_venue_reference_id ON venue(reference_id);