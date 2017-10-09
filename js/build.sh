#!/bin/bash
cd "`dirname $0`"
PATH="${PATH}:node_modules/.bin"
BUILD="$SHELL '$0'"

function run()
{
    echo "[$1]"

    case "$1" in
    clean)
        rm -rf lib dist generated-sources/test*
        ;;

    *)
        node build.js "$@" || exit $?
        ;;
    esac
}

run "$1"
