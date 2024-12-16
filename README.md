# Stable messaging library

This library implements Transactional Outbox and Transactional Inbox patterns, to provide stable messaging between distributed systems.

## Steps to set up library

1) Download this library code from repository
2) Build a jar using Maven/Gradle
3) Open your project and place there dependency:
   1. groupId: ua.kpi.ipze
   2. artifactId: message-lib 
   3. version: 1.0

4) Library is added to the classpath. It is needed to configure it.
5) Use `StableMessageConfigurer` class to configure library behaviour (see javadoc in there).
6) Create in your database `outbox` and `inbox` tables using this SQL:

    `create table inbox (` \
`  id                 uuid      not null primary key,` \
`   queue              varchar   not null,` \
`   message            text      not null,` \
`   received_date_time timestamp not null,` \
`   handled_date_time  timestamp` \
`);`

    `create table outbox (` \
`  id                 uuid      not null primary key,` \
`   queue              varchar   not null,` \
`   message            text      not null,` \
`   creation_date_time timestamp not null,` \
`   sent_date_time  timestamp` \
`);`

7) Implement `MessageReceiver` successors for each queue receiver. Don't forget to include it in `StableMessageConfigurerBuilder.messageReceiver()`.
8) Implement `Message sender` to provide an instrument for the library, for sending messages properly for desired message broker. Don't forget to include it in `StableMessageConfigurerBuilder.messageSender()`.
