IF "%1"=="" (
	SET /p zip="Path to Agent ZIP: " 
) ELSE ( 
	SET zip=%1
)

call :sub %zip%

START "" /wait cmd /c "copy %zip% .\src\main\resources\Bot.zip"
START "" /wait cmd /c "mvn clean package -Dfilename=Bot"
START "" /wait cmd /c "robocopy /e Template Bot"
START "" /wait cmd /c "copy .\target\starcraftgoalaiwrapper-0.0.1-SNAPSHOT-shaded.jar .\Bot\AI"
START "" /wait cmd /c "jar -cMf tournament%name%.zip -C Bot ."

::remove
START "" /wait cmd /c "rd target /S /Q"
START "" /wait cmd /c "rd Bot /S /Q"
START "" /wait cmd /c "del src\main\resources\Bot.zip /Q"

:sub
SET name=%~n1
exit /b