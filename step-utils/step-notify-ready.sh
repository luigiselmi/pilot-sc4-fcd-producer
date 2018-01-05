#!/bin/bash

if [ $ENABLE_INIT_DAEMON = "true" ] ;then
    echo "STEP ${INIT_DAEMON_STEP} : notify ready"
    CNT=0
    while true; do
        sleep 5
        CNT=$(($CNT + 5))
        echo "STEP ${INIT_DAEMON_STEP} : notify ready .. $CNT sec"
        string=$(curl -sL -w "%{http_code}" -X PUT $INIT_DAEMON_BASE_URI/finish?step=$INIT_DAEMON_STEP -o /dev/null)
        [ "$string" = "204" ] && break
    done
    echo "STEP ${INIT_DAEMON_STEP} : notify ready .. accepted"
fi
