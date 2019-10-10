#!/bin/bash

set -Eeuxo pipefail

. .env.development

PGPASSWORD=${APP_DB_PASS} psql -h ${APP_DB_HOST} -U ${APP_DB_USER}