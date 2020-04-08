#!/bin/bash
set -e

# test script used to emulate a command that has written to stdout, but has hung.
# A simple call to sleep in a test isn't sufficient, because listening on stdout/err needs to be done separately from the process waiting
# to avoid deadlocks.

while true
do
  echo "here's some stdout"
  sleep 3
done