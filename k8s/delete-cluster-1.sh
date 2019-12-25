#!/bin/bash

set -Eeuxo pipefail

eksctl --profile consensys delete cluster --name=kpen