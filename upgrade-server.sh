#!/bin/bash

set -Eeuxo pipefail

kubectl delete deploy/kpen-deployment || true
kubectl apply -f kpen-server-deployment.yaml

kubectl delete deploy/kworker-deployment || true
kubectl apply -f kworker-deployment.yaml