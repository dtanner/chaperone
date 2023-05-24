# chaperone is built on ubuntu:22.04

# NOTE: Please only use latest for testing. For actual deployments, you should use a named version.
# See https://github.com/dtanner/chaperone/releases for the latest version
FROM edgescope/chaperone:latest

# by default the container installs coreutils, bash, curl, and jq. but you'll typically add more tools for your situation.
# for example, you might want to add some python scripts in your checks
#RUN apt-get install -y python

# the default base config dir is configured as /chaperone.
COPY docker-files /chaperone
