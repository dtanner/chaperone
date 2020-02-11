# this container should only be built when the build.gradle.kts file changes
FROM gradle:jdk11 as cache
RUN mkdir -p /home/gradle/cache_home
ENV GRADLE_USER_HOME /home/gradle/cache_home
COPY build.gradle.kts /home/gradle/ignored-code/
WORKDIR /home/gradle/ignored-code/
RUN gradle clean build

# this container should be able to use the cached gradle dependencies from above
FROM gradle:jdk11 as builder
COPY --from=cache /home/gradle/cache_home /home/gradle/.gradle
COPY . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle check installDist


# this builds the runtime container that includes the compiled code
FROM adoptopenjdk/openjdk11:alpine
RUN apk add --no-cache coreutils bash curl jq dumb-init

COPY --from=builder /home/gradle/src/build/install/chaperone/ /opt/chaperone/

# add scripts we want globally available to the path
ADD global-scripts /usr/local/bin/

# add the sample config and checks directory to the default location as an example when running this docker image directly.
# the expectation is that usages will overwrite the /chaperone direectory with their real config and checks
ADD sample-config /chaperone

ENTRYPOINT [ "dumb-init", "--" ]
WORKDIR /opt/chaperone
CMD ["bin/chaperone"]
