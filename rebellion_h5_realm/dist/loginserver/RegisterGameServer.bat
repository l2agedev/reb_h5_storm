@echo off
title Game Server Registration...
:start
echo Starting Game Server Registration.
echo.
java -version:1.7 -server -Xms64m -Xmx64m -cp config:./libs/* l2r.loginserver.GameServerRegister

pause
