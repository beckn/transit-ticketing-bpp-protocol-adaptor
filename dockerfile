FROM openjdk:11-jre-slim
# try out alpine/debian

COPY build/libs/transit_ticketing_bpp_protocol-*.*.*-SNAPSHOT.jar /usr/local/lib/transit_ticketing_bpp_protocol.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /usr/local/lib/transit_ticketing_bpp_protocol.jar --spring.config.location=file:///usr/local/lib/application.yml"]
