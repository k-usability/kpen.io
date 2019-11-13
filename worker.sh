#!/bin/bash

set -Eeuxo pipefail

java -cp kpen-0.0.1.jar -Dloader.main=io.kpen.worker.Worker org.springframework.boot.loader.PropertiesLauncher