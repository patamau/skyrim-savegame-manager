# Skyrim Savegame Manager #
2012/03/06 0.2b
Matteo Pedrotti
patamau@gmail.com

### Changelog ###
> 0.2b
+ first publicly available version

### Requirements ###
Java Runtime Environment 1.5+ is required for the application to launch.
Any version of Skyrim is supported as far as I know.

### Installation ###
No installation is needed.
Just copy the .jar file wherever you prefer.
The .bat must be placed in the same folder of the .jar in order to work.

### Execution ###
Either double click on the jar (Javaw should handle this automatically).
If it doesn't work, launch the .bat file.

### Quick start ###
1) Customize your folder.
Open File -> Options... and verify the paths are correct.
You can customize the profiles folder as you prefer
but the Skyrim saves folder must point to the game saves folder
in order for the application to correctly identify the current saves.

2) Create profiles out of the current save games.
Select the "CURRENT" profile and click on the Backup button.
It should create a number of profiles, matching all your saves.
The profile will contain all the saves sharing the same character name.
All the profiles should be tagged as "current" since they are present
in the Skyrim save folder.

3) Deploy the preferred profile
Select a profile and click on Deploy.
Confirm you want to clear the current saves
so you'll only have one character active in the game saves folder.

4) Play Skyrim and create all the saves you want.
Quicksaves and autosaves will be recognized as well.
Remember to backup the current savegames before deploying a new profile
or you'll lose the progress.

### Notes ###
Be aware that the application is only considering the character name
when creating profiles: there is no other way (that I know of)
to univocally identify a character.