package com.mycompany.multiplayer_pong;

import com.almasb.fxgl.app.scene.FXGLMenu;
import com.almasb.fxgl.app.scene.SceneFactory;

/**
 *
 * @author Castiel Le
 */
public class MySceneFactory extends SceneFactory {
    
    @Override
    public FXGLMenu newMainMenu(){
        return new MultiplayerPongMainMenu();
    }
    
    @Override
    public FXGLMenu newGameMenu(){
        return new MultiplayerPongGameMenu();
    }
}
