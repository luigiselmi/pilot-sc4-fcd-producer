#!/bin/bash

if [ $ENABLE_INIT_DAEMON = "true" ] ;then
    echo "STEP ${INIT_DAEMON_STEP} : wait-for-step released"
    CNT=0
    while true; do
        sleep 5
        CNT=$(($CNT + 5))
        echo "STEP ${INIT_DAEMON_STEP} : wait-for-step released -- $CNT sec"
        string=$(curl -s $INIT_DAEMON_BASE_URI/canStart?step=$INIT_DAEMON_STEP)
        [ "$string" = "true" ] && break
    done
    echo "STEP ${INIT_DAEMON_STEP} : released, can start"
fi
