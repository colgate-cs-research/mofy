#/usr/bin/env bash

# Build batfish
git clone https://github.com/colgate-cs-research/batfish.git
cd batfish
source tools/batfish_functions.sh
batfish_build_all
