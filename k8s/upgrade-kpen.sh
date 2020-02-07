#!/bin/bash

set -Eeuxo pipefail

kubectl delete deploy/kpen-deployment || true
kubectl apply -f kpen/kpen-server-deployment.yaml

kubectl delete deploy/kworker-deployment || true
kubectl apply -f kpen/kworker-deployment.yaml