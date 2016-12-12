#!/bin/bash

while [ 1 ];
do
	PID=`ps -eo pid,args | grep GameServer_L2WOA-50x.sh | grep -v grep | awk {'print $1'}`
	PROC=`ps -eo pid,args | grep "-server -Xmx15G -Xms8G -Xmn128m -Xss32m" | grep -v grep | awk {'print $1'}`
	AUTOCLEAN=`ps -eo pid,args | grep autoclean_igbots-50x.sh | grep -v grep | awk {'print $1'}`
	CURRENT_DIR=`/bin/pwd |grep /home |awk {'print $1'}`

	if [ -z "$PID" ] || [ -z "$PROC" ]; then
		echo "Server is not running, killing AutoClean PID: $AUTOCLEAN" >> ${CURRENT_DIR}/log/autoclean_bans.log
		kill -9 ${AUTOCLEAN}
	else
		TOTAL_BOTS=`cat ${CURRENT_DIR}/lameguard/banned_hwid.txt |grep "IG bot\|IG Bot" | wc -l`
		/usr/bin/awk '!/IG bot|IG Bot/' ${CURRENT_DIR}/lameguard/banned_hwid.txt > temp && mv temp ${CURRENT_DIR}/lameguard/banned_hwid.txt
		/bin/sed '/^$/d' ${CURRENT_DIR}/lameguard/banned_hwid.txt > temp1 && mv temp1 ${CURRENT_DIR}/lameguard/banned_hwid.txt
		DATETIME=$(date | awk {'print $2,$3,$4'})
		echo "$DATETIME - Deleting ${TOTAL_BOTS} ig bots . . . | Sleeping 10 minutes." >> ${CURRENT_DIR}/log/autoclean_bans.log
		sleep 600;
	fi
done