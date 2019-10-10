#!/bin/bash

set -Eeuo pipefail

DEPLOYMENTS=`kubectl get deployments --all-namespaces | grep -v NAMESPACE`

while read -r line; do
    NS=`echo $line | awk '{print $1}'`
    N=`echo $line | awk '{print $2}'`
    kubectl delete -n $NS deployment $N
done <<< "$DEPLOYMENTS"

kubectl delete --all  pods || true
kubectl delete --all  services || true

kubectl get  pods --all-namespaces | grep -v "kube-system"