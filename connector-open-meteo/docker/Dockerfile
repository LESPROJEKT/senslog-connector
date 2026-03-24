#FROM maven:3.8.4-openjdk-17
FROM zenika/alpine-maven:3-jdk8 AS builder

ARG MAVEN_PROFILE
ARG CONFIG_FILE

ENV BUILD_PROFILE $MAVEN_PROFILE

COPY . /app/

COPY ./docker/start.sh /app/
COPY $CONFIG_FILE /app/config.yaml

WORKDIR /app

RUN mvn clean
RUN mvn package -P $MAVEN_PROFILE -DskipTests=true

FROM zenika/alpine-maven:3-jdk8 AS production

COPY --from=builder /app/bin/ /app/bin/
COPY --from=builder /app/config.yaml /app/config.yaml
COPY --from=builder /app/start.sh /app/start.sh

WORKDIR /app

ENTRYPOINT ["/bin/sh", "-C", "start.sh"]
