#!/bin/bash

while :;
do
	java -version:1.7 -server -Dfile.encoding=UTF-8 -Xmx64m -cp config:./libs/* l2r.loginserver.GameServerRegister

	[ $? -ne 2 ] && break
	sleep 10;
done
