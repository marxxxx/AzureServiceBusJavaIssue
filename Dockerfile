ARG BASE_IMAGE=openjdk:11-slim
FROM ${BASE_IMAGE}
VOLUME /tmp
COPY target/service-bus-receiver-sample-v2.jar /app.jar
ENTRYPOINT java \
    -Djdk.tls.client.protocols=TLSv1.2 \
    -XX:+UseSerialGC \
    -XX:+UseContainerSupport \
    -XX:+PerfDisableSharedMem \
    -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/dumps/oom.bin \
    -XX:MaxRAMPercentage=50 \
    -XX:MinHeapFreeRatio=20 \
    -XX:MaxHeapFreeRatio=40 \
    -Dsun.net.inetaddr.ttl=60 \
    -Djava.security.egd=file:/dev/./urandom \
    ${JVM_DEBUG_PORT:+-agentlib:jdwp=transport=dt_socket,address=$JVM_DEBUG_PORT,server=y,suspend=n} \
    -jar /app.jar
