if "%1"=="" set /p zip ="Path to Agent ZIP: "
else set zip=%1

START "" /wait cmd /c "copy %zip% .\src\main\resources\Bot.zip"
START "" /wait cmd /c "mvn clean package -Dfilename=Bot"
START "" /wait cmd /c "robocopy /e Template Bot"
START "" /wait cmd /c "copy .\target\starcraftgoalaiwrapper-0.0.1-SNAPSHOT-shaded.jar .\Bot\AI"
START "" /wait cmd /c "jar -cMf tournamentBot.zip -C Bot ."

#remove
START "" /wait cmd /c "rd target /S /Q"
START "" /wait cmd /c "rd Bot /S /Q"
START "" /wait cmd /c "del src\main\resources\Bot.zip /Q"
