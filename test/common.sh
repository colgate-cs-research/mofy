#!/usr/bin/env bash

export TEST_PASS=0
export TEST_FAIL=1

mofy_run() {
    java -jar "$MOFY_ROOT/target/mofy-1.0-jar-with-dependencies.jar" $@
}
