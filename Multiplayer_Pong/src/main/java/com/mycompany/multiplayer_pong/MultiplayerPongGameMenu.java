package com.mycompany.multiplayer_pong;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;

/**
 *
 * @author Castiel Le
 */
public class MultiplayerPongGameMenu extends FXGLMenu {

    public MultiplayerPongGameMenu() {
        super(MenuType.GAME_MENU);
        
        MultiplayerPongGameBtn newGameBtn = new MultiplayerPongGameBtn("Resume", () -> fireResume());
        MultiplayerPongGameBtn saveGameBtn = new MultiplayerPongGameBtn("Save", () -> MultiplayerPongApp.saveGame());
        MultiplayerPongGameBtn quitBtn = new MultiplayerPongGameBtn("Main Menu", () -> fireExitToMainMenu());

        var mainMenu = new VBox(15, newGameBtn, saveGameBtn, quitBtn);
        mainMenu.setAlignment(Pos.CENTER_LEFT);
        mainMenu.setTranslateX(80);
        mainMenu.setTranslateY(400);
        getContentRoot().getChildren().addAll(mainMenu);
    }

    
}
