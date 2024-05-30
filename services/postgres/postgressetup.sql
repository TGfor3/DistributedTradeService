CREATE DATABASE tradeserver;

\c tradeserver

CREATE TABLE events (
    poolId VARCHAR(32),
    eventID BIGINT,
    machineId BIGINT,
    -- eventType VARCHAR(32),
    eventKey VARCHAR(32),
    eventValue REAL,
    timestamp TIMESTAMP,
    -- PRIMARY KEY (poolId, eventID, eventKey, eventType)
    PRIMARY KEY (poolId, eventID, eventKey)
);

GRANT ALL PRIVILEGES ON DATABASE tradeserver TO myuser;
GRANT ALL PRIVILEGES ON TABLE events TO myuser;
