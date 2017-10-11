if not exist "./tournamentBots" mkdir "./tournamentBots"

IF "%1"=="" (
	SET /p zip="Path to Agent ZIP: " 
) ELSE ( 
	SET zip=%1
)

if not exist %zip% exit /B 404

call :sub %zip%

START "" /wait cmd /c "copy %zip% .\src\main\resources\Bot.zip"
START "" /wait cmd /c "mvn clean package -U"
START "" /wait cmd /c "robocopy /e Template Bot"
START "" /wait cmd /c "copy .\target\starcraftgoalaiwrapper-0.0.1-SNAPSHOT-shaded.jar .\Bot\AI"
START "" /wait cmd /c "copy NUL .\Bot\AI\%name%.dll"
START "" /wait cmd /c "jar -cMf .\tournamentBots\%name%.zip -C Bot ."

::remove
START "" /wait cmd /c "rd target /S /Q"
START "" /wait cmd /c "rd Bot /S /Q"
START "" /wait cmd /c "del src\main\resources\Bot.zip /Q"

:sub
SET name=%~n1
SET name=%name:~0,24%
exit /b