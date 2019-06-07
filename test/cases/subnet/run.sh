#/usr/bin/env bash

TEST_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$TEST_ROOT/common.sh"

TMP_DIR="$(mktemp -d)"

mofy_run -configs $TEST_DIR/configs -outputDir $TMP_DIR \
    -Modification Subnet -Percentage 100 -seed 1
diff -r $TEST_DIR/expected $TMP_DIR
