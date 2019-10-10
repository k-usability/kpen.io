#!/bin/bash

set -Eeuxo pipefail

kubectl apply -f https://raw.githubusercontent.com/kubernetes/ingress-nginx/master/deploy/static/mandatory.yaml
kubectl create -f nginx-ingress-controller.yaml
kubectl apply -f nginx-configuration.yaml
kubectl apply -f kpen-server-configuration.yaml