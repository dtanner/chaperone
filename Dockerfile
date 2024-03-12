# PREREQUISITE: the `./gradlew distTar` task must be run before building the docker image.

FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && apt-get install -y coreutils bash curl jq dumb-init procps

ADD build/distributions/chaperone.tar /opt/

ENTRYPOINT [ "dumb-init", "--" ]
WORKDIR /opt/chaperone
CMD ["bin/chaperone"]
