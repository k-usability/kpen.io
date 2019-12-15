#!/bin/bash

set -Eeuxo pipefail

kubectl apply -f nginx/mandatory.yaml
kubectl apply -f nginx/service-l7.yaml
kubectl apply -f nginx/patch-configmap-l7.yaml