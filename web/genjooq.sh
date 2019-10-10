#!/bin/bash

set -Eeuxo pipefail

rm -rf src/generated
psql -U postgres -h 127.0.0.01 -c 'drop database kpen' || true
psql -U postgres -h 127.0.0.01 -c 'create database kpen'
psql -U postgres -h 127.0.0.01 kpen < src/main/resources/schema.sql

./gradlew generateKpenJooqSchemaSource