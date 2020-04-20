FROM openjdk:8-jre-alpine

RUN mkdir -p /backend/lib 

RUN apk update && apk add bash
ADD /target/backend-1.0-SNAPSHOT-jar-with-dependencies.jar /backend/lib/backend.jar

#Start 
CMD [ "/usr/bin/java", "-jar", "/backend/lib/backend.jar"]
