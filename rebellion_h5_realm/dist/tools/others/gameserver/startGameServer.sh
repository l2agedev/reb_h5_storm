#!/bin/bash
####  PATH=/usr/bin:/usr/sbin:/sbin:/bin

PID=`ps -eo pid,args | grep GameServer_L2WOA-50x.sh | grep -v grep | awk {'print $1'}`
PROC=`ps -eo pid,args | grep "-server -Xmx15G -Xms8G -Xmn128m -Xss32m" | grep -v grep | awk {'print $1'}`
AUTOCLEAN=`ps -eo pid,args | grep autoclean_igbots-50x.sh | grep -v grep | awk {'print $1'}`

# TCP PORTS
# If IP's from different subnets are used, we can adjust |grep -e "IPpart1" -e "IPpart2" etc
PORT1=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -1 | tail -1`
PORT2=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -2 | tail -1`
PORT3=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -3 | tail -1`
PORT4=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -4 | tail -1`
PORT5=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -5 | tail -1`
PORT6=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -6 | tail -1`
PORT7=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -7 | tail -1`
PORT8=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -8 | tail -1`
PORT9=`netstat -tunlp |grep ${PROC} |grep "178.33" | awk {'print $4'} | sed -e 's/.*:/:/g' | head -9 | tail -1`

RATE="50x"

start() {
	if [ -z "$PID" ]; then
		sleep 1
		echo "L2Woa :: Starting Game Server $RATE... [DONE]"
		./GameServer_L2WOA-50x.sh &
	else
		echo "L2Woa :: GameServer $RATE is already running: PID: $PID | PROCESS: $PROC"
	fi

	if [ -z "$AUTOCLEAN" ]; then
		echo "L2Woa :: Starting AutoClean IGbot bans... [DONE]"
		./autoclean_igbots-50x.sh &
	else
		echo "L2WOa :: AutoClean is already running: PID: $AUTOCLEAN -- Killing and startig again."
		kill -9 ${AUTOCLEAN}
		./autoclean_igbots-50x.sh &
	fi
}

stop() {
	if [ -z "$PID" ] || [ -z "$PROC" ]; then
		echo "L2Woa :: GameServer $RATE is not started, cannot kill!"
	else
		echo "L2Woa :: GameServer $RATE killed: PID: $PID | PROCESS: $PROC"
		kill -9 ${PID} && sleep 1 && kill -9 ${PROC}
	fi

	if [ -z "$AUTOCLEAN" ]; then
		echo "L2Woa :: AutoClean is not running."
	else
		echo "L2Woa :: AutoClean killed: PID: $AUTOCLEAN"
		kill -9 ${AUTOCLEAN}
	fi
}

abort() {
	echo "L2Woa GameServer $RATE :: aborting restart."
	./tcontrol.py -c abort
}

shutdown120() {
	echo "L2Woa GameServer $RATE :: Shutting down in 120 seconds."
	./tcontrol.py -c shutdown -t 120
}

restart60() {
	echo "L2Woa GameServer $RATE :: Restarting in 60 seconds"
	./tcontrol.py -c restart -t 60
}

restart300() {
	echo "L2Woa GameServer $RATE :: Restarting in 300 seconds"
	./tcontrol.py -c restart -t 300
}

restart600() {
	echo "L2Woa GameServer $RATE :: Restarting in 600 seconds"
	./tcontrol.py -c restart -t 600
}

tstatus() {
	echo "L2Woa GameServer $RATE :: telnet status:"
	./tcontrol.py -c status
}

nondualbox() {
	netstat -n |grep -e "${PORT1}" -e "${PORT2}" -e "${PORT3}" -e "${PORT4}" -e "${PORT5}" -e "${PORT6}" -e "${PORT7}" -e "${PORT8}" -e "${PORT9}" |grep -o -E "[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+" |sort -u |wc -l
}

dualbox() {
	netstat -n |grep -e "${PORT1}" -e "${PORT2}" -e "${PORT3}" -e "${PORT4}" -e "${PORT5}" -e "${PORT6}" -e "${PORT7}" -e "${PORT8}" -e "${PORT9}" |grep -o -E "[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+" |wc -l
}

cpuusage() {
	ps S -p $PROC -o pcpu=
}

current-time() {
	date
}

machineUptime() {
	uptime |awk {'print $3,$4,$5'} |sed s/.$//
}

serverUptime() {
	ps -eo pid,etime,cmd|sort -n -k2 |grep $PROC |grep java | awk {'print $2'}
}

info() {
	echo "Today, $(current-time) -- Machine Uptime: $(machineUptime)"
	echo "L2Woa $RATE CPU Usage: $(cpuusage)%	| Online from: $(serverUptime)"
	echo "~Online users by TCP/IP connections:"
	echo "Ports in use by L2Woa $RATE: $PORT1, $PORT2, $PORT3, $PORT4, $PORT5, $PORT6, $PORT7, $PORT8, $PORT9"
	echo "Total Connections: $(dualbox)	| Unique Connections: $(nondualbox)"
}

case "$1" in
start)
	start
;;
abort)
	abort
;;
restart60)
	restart60
;;
restart300)
	restart300
;;
tstatus)
	tstatus
;;
shutdown120)
	shutdown120
;;
kill)
	stop
;;
info)
	info
;;
*)
	echo "Usage: $0 {start|restart60|restart300|abort|shutdown120|kill|tstatus|info}" >&2
	exit 1
;;
esac

exit 0