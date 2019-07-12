@echo off
@echo Running Roger

set ROGER_PATH=%~dp0
set VERSION=@version@
java -jar %ROGER_PATH%roger-%VERSION%.jar