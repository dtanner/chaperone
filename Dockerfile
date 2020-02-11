FROM gradle:jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle installDist


FROM adoptopenjdk/openjdk11:alpine-slim
RUN apk add --no-cache coreutils bash curl jq dumb-init

COPY --from=builder /home/gradle/src/build/install/chaperone/ /opt/chaperone/

# add scripts we want globally available to the path
ADD scripts /usr/local/bin/

# add the sample config and checks directory to the default location as an example when running this docker image directly.
# the expectation is that usages will overwrite the /chaperone direectory with their real config and checks
ADD sample-config /chaperone

ENTRYPOINT [ "dumb-init", "--" ]
WORKDIR /opt/chaperone
CMD ["bin/chaperone"]

