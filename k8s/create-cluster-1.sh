#!/bin/bash

set -Eeuxo pipefail

eksctl --profile consensys create cluster --name kpen2 --version 1.14 --nodegroup-name kpen-nodegroup --node-type t2.medium --nodes 3 --nodes-min 3 --nodes-max 3 --node-ami auto

kubectl apply -f dashboard/kubernetes-dashboard.yaml
kubectl apply -f dashboard/eks-service-account.yaml

kubectl apply -f nginx/mandatory.yaml
kubectl apply -f nginx/service-l7.yaml
kubectl apply -f nginx/patch-configmap-l7.yaml

kubectl apply -f kpen/kpen-server-deployment.yaml
kubectl apply -f kpen/kworker-deployment.yaml
kubectl apply -f kpen/kpen-service.yaml
kubectl apply -f kpen/kpen-ingress.yaml

