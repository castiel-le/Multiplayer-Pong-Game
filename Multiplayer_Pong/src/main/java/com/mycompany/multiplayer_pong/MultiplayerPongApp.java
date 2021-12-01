    package com.mycompany.multiplayer_pong;

/*
 * The MIT License (MIT)
 *
 * FXGL - JavaFX Game Library
 *
 * Copyright (c) 2015-2017 AlmasB (almaslvl@gmail.com)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */


import com.almasb.fxgl.animation.Interpolators;
import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.app.scene.MenuType;
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.ui.UI;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;

/**
 * A simple clone of Pong.
 * Sounds from https://freesound.org/people/NoiseCollector/sounds/4391/ under CC BY 3.0.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class MultiplayerPongApp extends GameApplication {
    private boolean isServer = false;
    
    private Connection<Bundle> connection;
    
    private Entity player1;
    private Entity player2;
    
    private Entity ball;
    
    private Input clientInput;

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("Pong");
        settings.setVersion("1.0");
        settings.setFontUI("pong.ttf");
        settings.addEngineService(MultiplayerService.class);
        settings.setSceneFactory(new SceneFactory() {
            @Override
            public MultiplayerPongMainMenu newMainMenu() {
                return new MultiplayerPongMainMenu();
            }

            @Override
            public MultiplayerPongGameMenu newGameMenu() {
                return new MultiplayerPongGameMenu();
            }
        });
        settings.setMainMenuEnabled(true);
        settings.setGameMenuEnabled(true);
    }

    private BatComponent player1Bat;
    private BatComponent player2Bat;

    protected void initServerInput() {
        getInput().addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                player1Bat.up();
            }

            @Override
            protected void onActionEnd() {
                player1Bat.stop();
            }
        }, KeyCode.W);

        getInput().addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                player1Bat.down();
            }

            @Override
            protected void onActionEnd() {
                player1Bat.stop();
            }
        }, KeyCode.S);
        
        clientInput = new Input();
        
        clientInput.addAction(new UserAction("Up") {
            @Override
            protected void onAction() {
                player2Bat.up();
            }

            @Override
            protected void onActionEnd() {
                player2Bat.stop();
            }
        }, KeyCode.W);

        clientInput.addAction(new UserAction("Down") {
            @Override
            protected void onAction() {
                player2Bat.down();
            }

            @Override
            protected void onActionEnd() {
                player2Bat.stop();
            }
        }, KeyCode.S);
    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("player1score", 0);
        vars.put("player2score", 0);
    }

    @Override
    protected void initGame() {
        runOnce(() -> {
            getDialogService().showConfirmationBox("Are you the host?", yes -> {
                isServer = yes;

                //Add background color to the game window.
                getGameScene().setBackgroundColor(Color.rgb(153, 204, 255));
                
                //this line is needed in order for entities to be spawned
                getGameWorld().addEntityFactory(new MultiplayerPongFactory());

                if (isServer) {
                    //Setup the TCP port that the server will listen at.
                    var server = getNetService().newTCPServer(7778);
                    server.setOnConnected(conn -> {
                        connection = conn;
                        
                        //Setup the entities and other necessary items on the server.
                        getExecutor().startAsyncFX(() -> onServer());
                    });
                    
                    //Start listening on the specified TCP port.
                    server.startAsync();
                    
                } else {
                    getDialogService().showInputBox("Enter Host IP:", x ->{
                    //Setup the connection to the server.
                        var client = getNetService().newTCPClient(x, 7778);
                        client.setOnConnected(conn -> {
                            connection = conn;
                        
                            //Enable the client to receive data from the server.
                            getExecutor().startAsyncFX(() -> onClient());
                        });
                    
                        //Establish the connection to the server.
                        client.connectAsync();
                    });
                }
            });
        }, Duration.seconds(0.5));
        getWorldProperties().<Integer>addListener("player1score", (old, newScore) -> {
            if (newScore == 11) {
                showGameOver("Player 1");
            }
        });

        getWorldProperties().<Integer>addListener("player2score", (old, newScore) -> {
            if (newScore == 11) {
                showGameOver("Player 2");
            }
        });

    }

    protected void initServerPhysics() {
        getPhysicsWorld().setGravity(0, 0);

        getPhysicsWorld().addCollisionHandler(new CollisionHandler(EntityType.BALL, EntityType.WALL) {
            @Override
            protected void onHitBoxTrigger(Entity a, Entity b, HitBox boxA, HitBox boxB) {
                if (boxB.getName().equals("LEFT")) {
                    inc("player2score", +1);
                } else if (boxB.getName().equals("RIGHT")) {
                    inc("player1score", +1);
                }

                play("hit_wall.wav");
                getGameScene().getViewport().shakeTranslational(5);
            }
        });

        CollisionHandler ballBatHandler = new CollisionHandler(EntityType.BALL, EntityType.PLAYER1) {
            @Override
            protected void onCollisionBegin(Entity a, Entity bat) {
                play("hit_bat.wav");
                playHitAnimation(bat);
            }
        };

        getPhysicsWorld().addCollisionHandler(ballBatHandler);
        getPhysicsWorld().addCollisionHandler(ballBatHandler.copyFor(EntityType.BALL, EntityType.PLAYER2));
    }

    @Override
    protected void initUI() {
        MainUIController controller = new MainUIController();
        UI ui = getAssetLoader().loadUI("main.fxml", controller);

        controller.getLabelScorePlayer().textProperty().bind(getip("player1score").asString());
        controller.getLabelScoreEnemy().textProperty().bind(getip("player2score").asString());

        getGameScene().addUI(ui);
    }

    private void initScreenBounds() {
        Entity walls = entityBuilder()
                .type(EntityType.WALL)
                .collidable()
                .buildScreenBounds(150);

        getGameWorld().addEntity(walls);
    }

//    private void initGameObjects() {
//        Entity ball = spawn("ball", getAppWidth() / 2 - 5, getAppHeight() / 2 - 5);
//        Entity bat1 = spawn("bat", new SpawnData(getAppWidth() / 4, getAppHeight() / 2 - 30).put("isPlayer", true));
//        Entity bat2 = spawn("bat", new SpawnData(3 * getAppWidth() / 4 - 20, getAppHeight() / 2 - 30).put("isPlayer", false));
//
//        playerBat = bat1.getComponent(BatComponent.class);
//    }

    private void playHitAnimation(Entity bat) {
        animationBuilder()
                .autoReverse(true)
                .duration(Duration.seconds(0.5))
                .interpolator(Interpolators.BOUNCE.EASE_OUT())
                .rotate(bat)
                .from(FXGLMath.random(-25, 25))
                .to(0)
                .buildAndPlay();
    }

    private void showGameOver(String winner) {
        
        
        String curveName = "secp256r1";
        KeyPair keypair = generateKeyPairECDSA(curveName);
        PrivateKey priv = keypair.getPrivate();
        String algorithm = "SHA1withECDSA";
        String message = "This is the message to be signed.";
        byte[] signature = generateSignature(algorithm, priv, message);
        writeByte(signature);
        
        
        
        
        getDialogService().showMessageBox(winner + " won! Demo over\nThanks for playing", getGameController()::exit);
    }

    public static void main(String[] args) {
        
        launch(args);
    }
    
    private void onServer() {
        
        initScreenBounds();
        initServerInput();
        initServerPhysics();
        
        //Spawn the player for the server
        ball = spawn("ball", new SpawnData(getAppWidth() / 2 - 5, getAppHeight() / 2 - 5).put("isServer", true));
        getService(MultiplayerService.class).spawn(connection, ball, "ball");
        player1 = spawn("bat", new SpawnData(getAppWidth() / 4, getAppHeight() / 2 - 30).put("isServer", true));
        getService(MultiplayerService.class).spawn(connection, player1, "bat");
        player2 = spawn("bat", new SpawnData(3 * getAppWidth() / 4 - 20, getAppHeight() / 2 - 30).put("isServer", true));
        getService(MultiplayerService.class).spawn(connection, player2, "bat");
        
        getService(MultiplayerService.class).addPropertyReplicationSender(connection, getWorldProperties());
        getService(MultiplayerService.class).addInputReplicationReceiver(connection, clientInput);
        
        player1Bat = player1.getComponent(BatComponent.class);
        player2Bat = player2.getComponent(BatComponent.class);
    }
     
     private void onClient(){
         getService(MultiplayerService.class).addEntityReplicationReceiver(connection, getGameWorld());
         getService(MultiplayerService.class).addInputReplicationSender(connection, getInput());
         getService(MultiplayerService.class).addPropertyReplicationReceiver(connection, getWorldProperties());
     }
     
     @Override
    protected void onUpdate(double tpf) {
        //checking if client is not null to not run the game without client
        if (isServer && (clientInput!=null)) {
            clientInput.update(tpf);
        }
    }
    
    KeyPair generateKeyPairECDSA(String curveName) {
        
        KeyPair keypair = null;
        try {
        ECGenParameterSpec ecParaSpec = new ECGenParameterSpec(curveName);
        
        /**
         * getInstance method of the key pair generator takes the label "EC"
         * and the Provider ("SunEC") for the Crypto schemes.
         */
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", "SunEC");
        generator.initialize(ecParaSpec);
        
        //Generate the key pair
        keypair = generator.genKeyPair();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e) {
            System.out.println("\nERROR occured while generating keypair.");
        }
        return keypair;
    }
    
    
    byte[] generateSignature (String algorithm, PrivateKey privatekey, String message){
        
        byte[] signature = null;
        try {
        //Create an instance of the signature scheme for the given signature algorithm
        Signature sig = Signature.getInstance(algorithm, "SunEC");
        
        //Initialize the signature scheme
        sig.initSign(privatekey);
        
        //Compute the signature
        sig.update(message.getBytes("UTF-8"));
        signature = sig.sign();
        
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeyException | UnsupportedEncodingException | SignatureException e) {
            System.out.println("\nERROR occured while generating signature.");
        }
        
        return signature;
    }
    
    
    static void writeByte(byte[] bytes)
    {
        try {
  
            // Initialize a pointer
            // in file using OutputStream
            OutputStream
                os
                = new FileOutputStream("PongApp.sig");
  
            // Starts writing the bytes in it
            os.write(bytes);
            System.out.println("Successfully"
                               + " byte inserted");
  
            // Close the file
            os.close();
        }
  
        catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
    
    
    
    
    


    
    
    
}