#!/bin/bash

set -Eeuxo pipefail

./dockerize.sh push
kubectl delete deploy/kpen-deployment || true
kubectl apply -f kpen-server-deployment.yaml
