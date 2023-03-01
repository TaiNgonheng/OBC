FROM adoptopenjdk/openjdk11:jre-11.0.10_9-alpine

ENV SERVICE_NAME=configuration

ARG APP_PATH=/app
ARG APP_USER=app
ARG APP_GROUP=app

ARG MS_DIR=microservices
ARG CW_AGENT_DIR=${MS_DIR}/cloudwatch-agent-prometheus
ARG TARGET_DIR=${MS_DIR}/${SERVICE_NAME}/target

RUN apk update \
    && apk add --no-cache --upgrade apk-tools busybox \
    && find / -xdev -perm /6000 -type f -exec chmod a-s {} \; || true \
    && addgroup -S ${APP_GROUP} && adduser -S ${APP_USER} -G ${APP_GROUP} \
    && mkdir -p ${APP_PATH} && chown -R ${APP_USER}:${APP_GROUP} ${APP_PATH}

WORKDIR ${APP_PATH}

ADD --chown=${APP_USER}:${APP_GROUP} \
    ${CW_AGENT_DIR}/jmx_prometheus_config.yaml \
    ${TARGET_DIR}/${SERVICE_NAME}.jar \
    ${CW_AGENT_DIR}/jmx_prometheus_javaagent-0.16.1.jar \
    ./

RUN apk update && apk add tzdata
ENV TZ=Asia/Phnom_Penh

USER app

ENTRYPOINT exec java $JAVA_OPTS $DEBUG_OPTS $SPRING_PROFILE -jar ${SERVICE_NAME}.jar