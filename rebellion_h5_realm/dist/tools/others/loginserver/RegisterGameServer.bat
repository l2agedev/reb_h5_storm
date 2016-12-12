@echo off
title Rebellion-Team: Game Server Registration...
:start
echo Starting Game Server Registration.
echo.
java -server -Xms64m -Xmx64m -Xbootclasspath/p:./jsr167.jar -cp config/xml;../libs/*; l2r.loginserver.GameServerRegister

pause
