#!/bin/bash

set -Eeuxo pipefail

eksctl create cluster --name kpen --version 1.13 --nodegroup-name kpen-nodegroup --node-type t2.medium --nodes 3 --nodes-min 3 --nodes-max 3 --node-ami auto