# Multiplayer Pong Game
Castiel Le
Alexander Arella Girardot
Edris Zoghlami
Mate Barabas

This program is a multiplayer pong game.

You will clone the repo onto your computer and then open it with netbeans.
Link: https://github.com/castiel-le/Multiplayer-Pong-Game
To run the program you will first have to right click the project then select the properties of the project in netbeans. Then go to the run tab and you will have to change the working directory to your own where the file is located on your computer.
Once the game is launched you will use the arrow keys and the "enter" key to make your selections in the main menu.
Once you have selected "New game" you will have an option to be the host. Then launch the game a second time as the guest where you will either input "localhost" or the ip of the host to connect. If you do not have a keystore file the game will prompt the host to input a password and then generate the keystore file. Once this is done the game will start running you can use "w","s" on the "qwerty" keyboard to move the paddles. You can pause the game on both client and server and navigate the pause menu with the same keys as the menu keys. (The save game option is only available for the host window the client cannot save the game.)
The game will end once a player reaches a score of 10, once it ends it will declare who the winner is and then proceed to create a signature file that is saved in the resources folder located in the main folder.

WARNING!! Do not start a new game on the same window as the previous one it will cause it to crash!
Do not enter the wrong save file name when you load a save file or else it will start a new game.
When first loading the game client score board will not be synced until the first score of each player.
