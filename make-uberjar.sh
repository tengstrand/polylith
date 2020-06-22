#!/usr/bin/env bash
set -e

if [[ $# -ne 2 ]]
  then
    echo "Usage: ENV_NAME MAIN_NS"
    exit 1
fi

mkdir -p classes
mkdir -p target

rm -rf classes/*
rm -rf target/$1.*

echo "Compiling environment"

clojure -A:aot/$1:env/$1

if [[ $? -ne 0 ]]
then
  echo "Could not compile environment."
  exit 1
fi

echo "Environment compiled. Creating an uberjar for the environment"

clojure -A:uberjar --aliases aot/$1:env/$1 --main-class $2 --target target/$1.jar

if [[ $? -ne 0 ]]
then
  echo "Could not create uberjar for the environment."
  exit 1
fi

echo "Uberjar created."
