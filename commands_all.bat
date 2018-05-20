for %%z in (*.zip) do (
  START "" /wait cmd /c "commands.bat %%z"
)