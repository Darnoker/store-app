CREATE TABLE inbox_messages (
    consumer_name VARCHAR(128) NOT NULL,
    event_id UUID NOT NULL,
    received_at TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (consumer_name, event_id)
);
