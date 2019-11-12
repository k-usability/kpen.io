#!/bin/bash

set -Eeuxo pipefail

. .env.development

PGSSLMODE=require PGPASSWORD=${APP_DB_PASS} psql -h ${APP_DB_HOST} -U ${APP_DB_USER} ${APP_DB_NAME}