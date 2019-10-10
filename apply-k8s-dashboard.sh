#!/bin/bash

set -Eeuxo pipefail

kubectl apply -f https://raw.githubusercontent.com/kubernetes/dashboard/v1.10.1/src/deploy/recommended/kubernetes-dashboard.yaml
kubectl apply -f eks-service-account.yaml