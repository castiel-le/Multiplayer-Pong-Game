package com.mycompany.multiplayer_pong;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.core.serialization.Bundle;
import static com.almasb.fxgl.dsl.FXGL.entityBuilder;
import static com.almasb.fxgl.dsl.FXGL.getAppHeight;
import static com.almasb.fxgl.dsl.FXGL.getAppWidth;
import static com.almasb.fxgl.dsl.FXGL.getGameScene;
import static com.almasb.fxgl.dsl.FXGL.getGameWorld;
import static com.almasb.fxgl.dsl.FXGL.getInput;
import static com.almasb.fxgl.dsl.FXGL.spawn;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;


public class MultiplayerPongApp extends GameApplication{
    
    private boolean isHost = false;
   
    private Connection<Bundle> connection;
    
    private BatComponent player1;
    
    private BatComponent player2;
    
    private Input clientInput;
    
    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Multiplayer Pong");
        settings.setVersion("1.0");
        settings.addEngineService(MultiplayerService.class);
    }
    
    @Override
    protected void initInput() {
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                player1.up();
            }

            @Override
            protected void onActionEnd() {
                player1.stop();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                player1.down();
            }

            @Override
            protected void onActionEnd() {
                player1.stop();
            }
        }, KeyCode.S);
        
        clientInput = new Input();
        
    }
    
    @Override
    protected void initGame(){
        runOnce(() -> {
            getDialogService().showConfirmationBox("Are you the host?", yes -> {
                isServer = yes;

                //Add background color to the game window.
                getGameScene().setBackgroundColor(Color.rgb(153, 204, 255));
                
                //this line is needed in order for entities to be spawned
                getGameWorld().addEntityFactory(new BasicGameFactory());

                if (isServer) {
                    //Setup the TCP port that the server will listen at.
                    var server = getNetService().newTCPServer(7777);
                    server.setOnConnected(conn -> {
                        connection = conn;
                        
                        //Setup the entities and other necessary items on the server.
                        getExecutor().startAsyncFX(() -> onServer());
                    });
                    
                    //Start listening on the specified TCP port.
                    server.startAsync();
                    
                } else {
                    //Setup the connection to the server.
                    var client = getNetService().newTCPClient("localhost", 7777);
                    client.setOnConnected(conn -> {
                        connection = conn;
                        
                        //Enable the client to receive data from the server.
                        getExecutor().startAsyncFX(() -> onClient());
                    });
                    
                    //Establish the connection to the server.
                    client.connectAsync();
                }
            });
        }, Duration.seconds(0.5));
    }


    
    private void initGameObjects() {
        Entity ball = spawn("ball", getAppWidth() / 2 - 5, getAppHeight() / 2 - 5);
        Entity bat1 = spawn("bat", new SpawnData(getAppWidth() / 4, getAppHeight() / 2 - 30).put("isServer", true));
        Entity bat2 = spawn("bat", new SpawnData(3 * getAppWidth() / 4 - 20, getAppHeight() / 2 - 30).put("isServer", false));

        player1 = bat1.getComponent(BatComponent.class);
        player2 = bat2.getComponent(BatComponent.class);
    }
}
    
