create table inbox
(
    id                 uuid      not null primary key,
    queue              varchar   not null,
    message            text      not null,
    received_date_time timestamp not null,
    handled_date_time  timestamp
);


create table outbox
(
    id                 uuid      not null primary key,
    queue              varchar   not null,
    message            text      not null,
    creation_date_time timestamp not null,
    sent_date_time     timestamp
);

