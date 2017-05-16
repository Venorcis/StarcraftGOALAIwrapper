START "" /wait cmd /c "copy .\ExampleBotZerg.zip .\src\main\resources\Bot.zip"
START "" /wait cmd /c "mvn install -Dfilename=Bot"
START "" /wait cmd /c "robocopy /e Template Bot"
START "" /wait cmd /c "copy .\target\starcraftgoalaiwrapper-0.0.1-SNAPSHOT-shaded.jar .\Bot\AI"
START "" /wait cmd /c "jar -cMf tournamentBot.zip -C Bot ."

#remove
START "" /wait cmd /c "rd target /S /Q"
START "" /wait cmd /c "rd Bot /S /Q"
START "" /wait cmd /c "del src\main\resources\Bot.zip /Q"