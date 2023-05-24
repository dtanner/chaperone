# chaperone is built on ubuntu:22.04
# make sure to check that the chaperone image version here is the latest referenced on the github releases
FROM edgescope/chaperone:1.6.0

# by default the container installs coreutils, bash, curl, and jq. but you'll typically add more tools for your situation.
# for example, you might want to add some python scripts in your checks
#RUN apt-get install -y python

# the default base config dir is configured as /chaperone.
COPY docker-files /chaperone
