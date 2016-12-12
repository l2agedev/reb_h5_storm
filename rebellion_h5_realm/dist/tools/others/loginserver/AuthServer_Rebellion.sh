#!/bin/bash
PATH=/usr/bin:/usr/sbin:/sbin:/bin

MAINPID=`ps -eo pid,args | grep AuthServer_Rebellion.sh | grep -v grep | awk {'print $1'}`
PROCESS=`ps -eo pid,args | grep "java -server -Dfile.encoding=UTF-8 -Xmx64m -cp config:./libs/*" | grep -v grep | awk {'print $1'}`

start() {
        if [ -z "$MAINPID" ]; then
                sleep 1
                echo "Starting Rebellion LoginServer... [DONE]";
                ./AuthServer_Rebellion.sh &
        else
                echo "Rebellion LoginServer is already running: PID: $MAINPID | PROCESS: $PROCESS";
        fi
}

stop() {
        kill -9 $MAINPID && sleep 2 && kill -9 $PROCESS
        echo "Rebellion LoginServer killed: PID: $MAINPID | PROCESS: $PROCESS"
}

case "$1" in
start)
        start
;;
kill)
        stop
;;
*)
        echo "Usage: $0 {start|kill}" >&2
        exit 1
;;
esac

exit 0
