#!/bin/bash

set -Eeuxo pipefail

kubectl create -f kpen-loadbalancer-deployment.yaml
kubectl apply -f kpen-server-deployment.yaml