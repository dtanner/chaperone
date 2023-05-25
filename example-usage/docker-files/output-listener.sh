#!/usr/bin/env bash

echo "in output-listener"

json=$1

name=$(echo "$json" | jq -r '.name')
status=$(echo "$json" | jq -r '.status')
tags=$(echo "$json" | jq -r '.tags')
output=$(echo "$json" | jq -r '.output')

echo "Name: $name"
echo "Status: $status"
echo "Tags: $tags"
echo "Output: $output"

