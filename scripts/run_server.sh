#!/bin/bash
SERVER_PORT=$1
THREADS=$2
SINGLETHREADED=$3
LOAD_INTENSITY=$4
LOAD_VECTOR_SIZE=$5
NUM_SELECTORS=$6
DURATION=$7

cd /work/tests/gameserver/src/out
screen -d -m java -Dsystem.killtime=${DURATION} -Dnetwork.port=${SERVER_PORT} -Dsystem.numthreads=${THREADS} -Dsystem.numselectors=${NUM_SELECTORS} -Dsystem.singlethreaded=${SINGLETHREADED} -Dload.intensity=${LOAD_INTENSITY} -Dload.vectorsize=${LOAD_VECTOR_SIZE} server.NetworkSelector
