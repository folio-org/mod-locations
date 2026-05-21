FROM folioci/alpine-jre-openjdk21:latest

USER root
RUN apk upgrade --no-cache
USER folio

ENV APP_FILE mod-locations-fat.jar
ENV JAR_FILE=mod-locations-server/target/${APP_FILE}
COPY ${JAR_FILE} ${JAVA_APP_DIR}/${APP_FILE}

EXPOSE 8081
