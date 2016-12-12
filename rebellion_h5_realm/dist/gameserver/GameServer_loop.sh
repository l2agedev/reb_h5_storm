#!/bin/bash

while :; do
	[ -f log/java.log.0 ] && mv log/java.log.0 "log/`date +%Y-%m-%d_%H-%M-%S`_java.log"
	[ -f log/stdout.log ] && mv log/stdout.log "log/`date +%Y-%m-%d_%H-%M-%S`_stdout.log"
	[ -f log/chat.log ] && mv log/chat.log "log/`date +%Y-%m-%d_%H-%M-%S`-chat.log"
	java \
	-version:1.7 \
	-server \
	-Xmx4G \
	-Xms2G \
	-Xmn128m \
	-Dfile.encoding=UTF-8 \
	-cp config:./libs/* l2r.gameserver.GameServer > log/stdout.log 2>&1
	[ $? -ne 2 ] && break
	sleep 10;
done