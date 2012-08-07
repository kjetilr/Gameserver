#!/bin/bash
INSTANCES=$1
DELAY=$2 
SERVER_PORT=$3
THREADS=$4
NUM_SELECTORS=$5
DURATION=$6
SERVER="jefferson.ndlab.net"
## Used to run PlaybackClient on each client machine
## for gameserver experiments
cd /home/tests/gameserver/src/out
screen -d -m java -Dplayback.startnumber=$INSTANCES -Dplayback.logondelay=$DELAY -Dsystem.killtime=${DURATION} -Dsystem.tracefilename=../logs/5min-5players.log -Dnetwork.port=${SERVER_PORT} -Dsystem.numselectors=${NUM_SELECTORS} -Dnetwork.serverurl=${SERVER} -Dplayback.maxRetryTime=16 -Dsystem.numthreads=${THREADS} testrig.PlaybackClient

