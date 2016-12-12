#!/bin/bash

while :;
do
	java -Xbootclasspath/p:./jsr167.jar -server -Dfile.encoding=UTF-8 -Xmx64m -cp config:./libs/* l2r.loginserver.GameServerRegister

	[ $? -ne 2 ] && break
	sleep 10;
done
