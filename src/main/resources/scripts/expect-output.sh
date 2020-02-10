#!/usr/bin/env bash

set -eEuo pipefail

function error() {
  >&2 echo "$@"
}

function usage() {
  error "usage: your_command | $0 expected_output"
  error "e.g. curl -sL -w '%{http_code}' -o /dev/null 'https://httpbin.org/status/200' | $0 200"
}

if [[ $# -ne 1 ]]; then
    usage
    exit 2
fi

expected_output=$1
actual_output=$(</dev/stdin)

if [ "$expected_output" != "$actual_output" ]; then
  echo "expected output doesn't match actual. expected=[$expected_output], actual=[$actual_output]"
  exit 1
fi
