#!/bin/bash

set -Eeuxo

POD=`kubectl get pods | grep deployment | awk '{print $1}'`
kubectl logs --follow $POD