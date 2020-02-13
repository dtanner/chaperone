#!/bin/bash

set -e
set -o pipefail

# Test script used to simulate failure rate at the given percentage.
# e.g. If you want this script's exit code to indicate error approximately 1% of the time it's called, send an argument of 1.

fail_percent=$1

random_number=$(shuf -i 1-100 -n1)

if [ "$fail_percent" -ge "$random_number" ]; then
    echo "simulated fail"
    exit 1
else
  echo "simulated pass"
fi
