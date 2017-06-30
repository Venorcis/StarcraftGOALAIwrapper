# Instructions
1) Place a ZIP file with only your agent code in the repo's main directory. This ZIP file can contain at most 1 MAS2G file (be aware of possible __MACOSX or .git folders cloning files; remove them).
2) Run 'commands.bat' with the name of your ZIP file as the first and only argument. For this you need Maven (and thus a JDK) installed and available on your path.
3) The output will be a new ZIP file (with its name possibly corrected for use in StarCraft) in the 'tournamentBots' directory (that is created if it does not exist yet); this ZIP file contains a runnable JAR (and according BAT file) which runs the given agent code with the latest version of the GOAL runtime and the StarCraft environment.

For _SSCAIT_ submissions, in the resulting ZIP file, move the contents of the AI directory to the ZIP's main directory, remove the run_proxy.bat file, and add the BWAPI.dll file from the repo's main directory. Select 'JAR made with JNIBWAPI (Java)' in the submission form at http://sscaitournament.com/index.php?action=submit
