#!/bin/bash

while :; do
	[ -f log/java.log.0 ] && mv log/java.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	java \
	-server \
	-Xmx15G \
	-Xms8G \
	-Xmn128m \
	-Xss32m \
	-XX:+UseThreadPriorities \
	-XX:+MaxFDLimit \
	-XX:+AggressiveOpts \
	-XX:+UseBiasedLocking \
	-XX:+UseFastAccessorMethods \
	-XX:+UseStringCache \
	-XX:+OptimizeStringConcat \
	-Dfile.encoding=UTF-8 \
	-cp config:./libs/lameguard-1.9.5.jar:./libs/* com.lameguard.LameGuard l2r.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
	sleep 10;
done