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
import com.almasb.fxgl.app.scene.SceneFactory;
import com.almasb.fxgl.core.math.FXGLMath;
import com.almasb.fxgl.core.serialization.Bundle;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;
import com.almasb.fxgl.ui.UI;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.io.File;

import org.apache.commons.io.FileUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.*;
import java.security.spec.ECGenParameterSpec;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.text.Normalizer.Form;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.Map;

import static com.almasb.fxgl.dsl.FXGL.*;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.multiplayer.MultiplayerService;
import com.almasb.fxgl.net.Connection;
import java.math.BigInteger;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import java.text.Normalizer;
import java.time.Instant;
import java.util.Date;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.SubjectKeyIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.DigestCalculator;
import org.bouncycastle.operator.bc.BcDigestCalculatorProvider;
import org.bouncycastle.asn1.oiw.OIWObjectIdentifiers;
import org.bouncycastle.asn1.x509.AuthorityKeyIdentifier;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.CertIOException;
import org.bouncycastle.cert.X509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.scene.control.PasswordField;
import javax.crypto.SecretKey;

/**
 * A simple clone of Pong.
 * Sounds from https://freesound.org/people/NoiseCollector/sounds/4391/ under CC BY 3.0.
 *
 * @author Almas Baimagambetov (AlmasB) (almaslvl@gmail.com)
 */
public class MultiplayerPongApp extends GameApplication {
    private boolean isServer = false;
    private boolean didKeyStoreNotExist;
    private Connection<Bundle> connection;

    private static CryptoKeyStore cks;
    
    private Entity player1;
    private Entity player2;
    
    private Entity ball;
    
    private Input clientInput;

    private boolean pauseState = false;

    private boolean validLoad = false;

    private boolean doneOnce = false;

    private static final String ALGO = "AES/GCM/NoPadding";

    private final String KEYSTORE_PATH = "src/main/resources/assets/keystore/keystore.p12";

    private final String APPPATH = "src/main/java/com/mycompany/multiplayer_pong/MultiplayerPongApp.java";
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

    protected void initClientInput(){
        onKeyDown(KeyCode.ESCAPE, () ->{
            pauseState = true;
            var pauseBundle = new Bundle("pauseState");
            pauseBundle.put("pauseState", pauseState);
            connection.send(pauseBundle);
        });
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
                    File keystore = new File(KEYSTORE_PATH);
                    if(!keystore.exists()){
                        notExistKeyStore();
                    }
                    else{
                        validateSig();
                    }
                    getDialogService().showConfirmationBox("Do you want to load old games?", load -> {
                        if(load){
                            while(!validLoad){
                                try{
                                    loadSavedGame();
                                    validLoad = true;
                                }
                                catch (Exception e){
                                    e.printStackTrace();
                                }
                            }
                        } else{
                            System.out.print("new");
                        }
                    });
                    //Setup the TCP port that the server will listen at.
                                                            // Port 7777 did not work on some machines
                    var server = getNetService().newTCPServer(7778);
                    server.setOnConnected(connection -> {
                        syncPause(connection);
                        //Setup the entities and other necessary items on the server.
                        getExecutor().startAsyncFX(() -> onServer());
                    });
                    // storing keystore

                    //Start listening on the specified TCP port.
                    server.startAsync();


                } else {
                    javafx.scene.control.TextField input = new javafx.scene.control.TextField();
                    javafx.scene.control.Button submit = new Button("Enter");
                        //normalizing x which is the ip input
                        submit.setOnAction(e -> {
                            normalizeIP(input.getText());
                            var checkCon = validateConnection(input.getText());
                            if(checkCon){
                                //Setup the connection to the server.
                                var client = getNetService().newTCPClient(input.getText(), 7778);
                                client.setOnConnected(connection -> {
                                    syncPause(connection);
                                    //Enable the client to receive data from the server.
                                    getExecutor().startAsyncFX(() -> onClient());
                                });

                                //Establish the connection to the server.
                                client.connectAsync();
                            }else {
                                getDialogService().showBox("Re-enter IP: ", input, submit);
                            }
                        });
                    getDialogService().showBox("Enter IP: ", input, submit);
                }
            });
        }, Duration.seconds(0.5));
        getWorldProperties().<Integer>addListener("player1score", (old, newScore) -> {
            if (newScore == 11) {
                try {
                    showGameOver("Player 1");
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (SignatureException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        });

        getWorldProperties().<Integer>addListener("player2score", (old, newScore) -> {
            if (newScore == 11) {
                try {
                    showGameOver("Player 2");
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                } catch (SignatureException e) {
                    e.printStackTrace();
                } catch (NoSuchProviderException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                }
            }
        });

    }
    
    /**
     * Method for normalizing an ordinary string
     * @param s
     * @return 
     */
    private String normalizeString(String s) {
        return Normalizer.normalize(s, Form.NFKC);
    }

    /**
     * Method for validating connection.
     * @param x
     * @return 
     */
    private boolean validateConnection(String x) {
        Socket socket = new Socket();
        try {
            socket.connect(new InetSocketAddress(x, 7778), 2000);
            socket.close();
            return true;
        } catch (SocketTimeoutException e) {
            return false;
        } catch (IOException ioException) {
            return false;
        }
    }

        /**
     * method that checks and normalizes the user input of the ip address
     * then proceeds to check for illegal patterns and if it matches the ip address pattern.
     * It normalizes it to NFKC form.
     * @param ip string that is the ip
     * @return normalized version of the ip address
     */
    protected String normalizeIP(String ip) throws IllegalArgumentException {
        String normalized = Normalizer.normalize(ip, Form.NFKC);
        Pattern pattern1 = Pattern.compile("[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}");
        Matcher matcher1 = pattern1.matcher(normalized);
        boolean matches1 = matcher1.matches();
        
        Pattern pattern2 = Pattern.compile("[L-l][O-o][C-c][A-a][L-l][H-h][O-o][S-s][T-t]");
        Matcher matcher2 = pattern2.matcher(normalized);
        boolean matches2 = matcher2.matches();
        if(matches1 || matches2){
            System.out.println("Input string is acceptable");
        }
        else{
            System.out.println("Black listed character found in input and does not match IP pattern!!");
            getDialogService().showMessageBox("Connection Timeout!");
        }
        return normalized;
    }

    private void syncPause(Connection<Bundle> connection) {
        this.connection = connection;
        connection.addMessageHandlerFX((conn, message) -> {
            if(message.exists("pauseState")){
                pauseState = message.get("pauseState");
                if(pauseState){
                    getExecutor().startAsyncFX(() -> getGameController().pauseEngine());
                }
                else{
                    getExecutor().startAsyncFX(() -> getGameController().resumeEngine());
                }
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

    private void showGameOver(String winner) throws UnrecoverableKeyException, CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException, SignatureException, NoSuchProviderException, InvalidKeyException {
        if(isServer) {
            FileSignature.generateSignature(cks.getPrivateKey(), APPPATH);
        }
        getDialogService().showMessageBox(winner + " won! Demo over\nThanks for playing", getGameController()::exit);
    }

    public static void main(String[] args) {
        
        
        launch(args);
    }
    
    private void onServer() {

        if(!doneOnce) {
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

            doneOnce = true;
        }
    }
     
     private void onClient(){

        initClientInput();

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

        if (!isServer && pauseState){
            pauseState = false;
            var pauseBundle = new Bundle("pauseState");
            pauseBundle.put("pauseState", pauseState);
            connection.send(pauseBundle);
        }
    }

    //in progress
    @Override
    protected void onPreInit() {
        getSaveLoadService().addHandler(new SaveLoadHandler() {
            @Override
            public void onSave(DataFile dataFile) {
                var savedBundle = new Bundle("GameData");
                IntegerProperty player1score = getip("player1score");
                IntegerProperty player2score = getip("player2score");
                savedBundle.put("player1score", player1score.get());
                savedBundle.put("player2score", player2score.get());
                dataFile.putBundle(savedBundle);
            }

            @Override
            public void onLoad(DataFile dataFile) {
                var savedBundle = dataFile.getBundle("GameData");

                int player1score = savedBundle.get("player1score");
                int player2score = savedBundle.get("player2score");

                set("player1score", player1score);
                set("player2score", player2score);
            }
        });
    }
    //Custom load game
    public void loadSavedGame(){
        getDialogService().showInputBox("Enter Saved Game's Name", savedName -> {
            String encryptsavedPath = savedName + ".enc";
            File encryptedsaveFile = new File(encryptsavedPath);
            boolean saveExists = false;
            if (encryptedsaveFile.exists()) {
                saveExists = true;
            }
            if (saveExists) {
                String savedPath = savedName + ".sav";
                File savedFile = new File(savedPath);
                try {
                    cks.decryptFile(ALGO, cks.getSecretKey(), encryptedsaveFile, savedFile);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (NoSuchPaddingException e) {
                    e.printStackTrace();
                } catch (InvalidKeyException e) {
                    e.printStackTrace();
                } catch (InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (CertificateException e) {
                    e.printStackTrace();
                } catch (UnrecoverableKeyException e) {
                    e.printStackTrace();
                } catch (KeyStoreException e) {
                    e.printStackTrace();
                }
                getSaveLoadService().readAndLoadTask(savedPath).run();
            } else {
                System.out.println("Save file not found");
            }
        });
    }
    // Custom save game
    public static void saveGame(){
        getDialogService().showInputBox("Enter Save Name:", savedName -> {
            String savedPath = savedName + ".sav";
            getSaveLoadService().saveAndWriteTask(savedPath).run();
            File saveFile = new File(savedPath);
            File encryptedFile = new File(savedName + ".enc");
            try {
                cks.encryptFile(ALGO, cks.getSecretKey(), saveFile, encryptedFile);
                saveFile.delete();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            }
        });
    }

    public void notExistKeyStore(){
        String pwd = getPwd();
        try {
            cks = new CryptoKeyStore(pwd);
            cks.buildKeyStore();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (CertificateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private String getPwd(){
        javafx.scene.control.PasswordField passwordField = new PasswordField();
        var submitPassword = new javafx.scene.control.Button("Enter");
        String prompt = "Enter your password:";
        getDialogService().showBox(prompt, passwordField, submitPassword);
        return passwordField.getText();
    }
    public void validateSig(){
        String pwd = getPwd();
        var validPwd = false;
        while(!validPwd){
            try {
                cks = new CryptoKeyStore(pwd);
                FileSignature.verifySignature(cks.getPublicKey(), APPPATH);
                validPwd = true;
            } catch (CertificateException e) {
                e.printStackTrace();
            } catch (KeyStoreException e) {
                e.printStackTrace();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (UnrecoverableKeyException e) {
                e.printStackTrace();
            } catch (SignatureException e) {
                e.printStackTrace();
            } catch (NoSuchProviderException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            }
        }
    }
}

