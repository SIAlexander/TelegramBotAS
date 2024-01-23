-- liquibase formatted sql

-- changeset asmokvin:1
CREATE table notification_task
(
    id           BIGSERIAL PRIMARY KEY,
    id_chat      BIGINT,
    notification TEXT,
    date_time    TIMESTAMP
);