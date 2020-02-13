# chaperone is built on debian-slim
FROM edgescope/chaperone:latest

# by default the container installs coreutils, bash, curl, and jq
# for example, you might want to add some python scripts in your checks
#RUN apt-get install -y python

# the default base config dir is /chaperone.
COPY docker-files /chaperone
