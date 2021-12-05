package com.mycompany.multiplayer_pong;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.dsl.FXGL;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 *
 * @author Castiel Le
 */
public class MultiplayerPongMainMenu extends FXGLMenu {

    public MultiplayerPongMainMenu() {
        super(MenuType.MAIN_MENU);

        MultiplayerPongMainBtn startGameBtn = new MultiplayerPongMainBtn("Start", () -> fireNewGame());
        MultiplayerPongMainBtn quitBtn = new MultiplayerPongMainBtn("Quit", () -> fireExit());

        var mainMenu = new VBox(15, startGameBtn, quitBtn);
        mainMenu.setAlignment(Pos.CENTER_LEFT);
        mainMenu.setTranslateX(80);
        mainMenu.setTranslateY(400);
        getContentRoot().getChildren().addAll(mainMenu);
    }
}
