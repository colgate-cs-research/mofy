#!/usr/bin/env bash

export TEST_ROOT="$(cd "$(dirname "$0")" && pwd)"
export MOFY_ROOT="$(dirname $TEST_ROOT)"

source "$TEST_ROOT/common.sh"

RED=$(tput setaf 1)
GREEN=$(tput setaf 2)
NORMAL=$(tput sgr0)
print_log_line() {
    LINE="==========================================================================="
    printf "=== %s %s\n" "$1" "${LINE:${#1}}"
}

TEST_COUNT=0
PASS_COUNT=0
while read TEST_NAME; do
    print_log_line $TEST_NAME
    TEST_COUNT=$((TEST_COUNT+1))
    TEST_DIR="$TEST_ROOT/cases/$TEST_NAME"

    $TEST_DIR/run.sh
    RESULT=$?

    if [ $RESULT -eq $TEST_PASS ]; then
        print_log_line "$GREEN PASS $NORMAL"
        PASS_COUNT=$((PASS_COUNT+1))
    else
        print_log_line "$RED FAIL $NORMAL"
    fi
done <<<$(ls $TEST_ROOT/cases/)

if  [ $PASS_COUNT -eq $TEST_COUNT ]; then
    echo $GREEN
    FINAL_RESULT=$TEST_PASS
else
    echo $RED
    FINAL_RESULT=$TEST_FAIL
fi
echo $PASS_COUNT of $TEST_COUNT tests passed$NORMAL
exit $FINAL_RESULT
