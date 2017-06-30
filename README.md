# StarcraftGOALAIwrapper
A single-JAR wrapper to use a GOAL agent in the SSCAIT competition

Instructions:
1) Place a ZIP file with only your agent code in the repo's main directory. This ZIP file can contain at most 1 .mas2g file (be aware of possible __MACOSX or .git folders cloning files).
2) Run 'commands.bat' with the name of your ZIP file as the first and only argument. For this you need Maven (and thus a JDK) installed.
3) The output will be a new ZIP file (with its name possibly corrected for use in StarCraft) in a 'tournamentBots' directory (that is created if it does not exist yet); this ZIP packages the given agent code with the latest version of GOAL and the StarCraft environment for use in competitions like SSCAIT.
