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
import com.almasb.fxgl.profile.DataFile;
import com.almasb.fxgl.profile.SaveLoadHandler;
import com.almasb.fxgl.ui.UI;
import javafx.beans.property.IntegerProperty;
import javafx.scene.control.Button;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import org.apache.commons.io.FileUtils;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
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
import java.text.Normalizer.Form;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.IOException;
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
import java.util.Scanner;
    
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
    
import com.mycompany.multiplayer_pong.CryptoUtility;
import java.security.KeyStore;

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
    
    private KeyStore ks;
    
    private Entity ball;
    
    private Input clientInput;

    private boolean pauseState = false;

    private boolean validLoad = false;

    private boolean doneOnce = false;

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
                    
                    File keyStoreFile = new File("src\\main\\resources\\keystore.p12");
                    if(keyStoreFile.exists() && !keyStoreFile.isDirectory()){
                       
                    }

                    else{

                        KeyPair keyPair = generateKeyPairECDSA("secp256r1");
                        PrivateKey priv = keyPair.getPrivate();
                    }
                    
                    File signatureFile = new File("src\\main\\resources\\PongApp.sig");
                    if(signatureFile.exists() && !signatureFile.isDirectory()){
                        byte[] fileSignature = getByteArrayFromFile("src\\main\\resources\\PongApp.sig");
                    }
                    
                    else{
                        byte[] fileSignature = new byte[0];
                    }
                    
                    
                             

                    
                    // TODO : prompt keystore if doesn't exist, or if user saves/loads
                    //  CryptoUtility ks = new CryptoUtility(ksPassword); 
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
                    var server = getNetService().newTCPServer(7778);
                    server.setOnConnected(connection -> {
                        syncPause(connection);
                        //Setup the entities and other necessary items on the server.
                        getExecutor().startAsyncFX(() -> onServer());
                    });
                    
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
                showGameOver("Player 1");
            }
        });

        getWorldProperties().<Integer>addListener("player2score", (old, newScore) -> {
            if (newScore == 11) {
                showGameOver("Player 2");
            }
        });

    }

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

    private void showGameOver(String winner) {
        
        File pongAppJava = new File("src\\main\\java\\com\\mycompany\\multiplayer_pong\\MultiplayerPongApp.java");
        Scanner read = null;
        String message = "";
        try {
            read = new Scanner(pongAppJava);
            while (read.hasNext()) {
                message += read.nextLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
            read.close();
        }
        String curveName = "secp256r1";
        File keyStoreFile = new File("src\\main\\resources\\keystore.p12");
        if(keyStoreFile.exists() && !keyStoreFile.isDirectory()){

        }

        else{

            KeyPair keyPair = generateKeyPairECDSA(curveName);
            PrivateKey priv = keyPair.getPrivate();
        }

        KeyPair keyPair = generateKeyPairECDSA(curveName);
        PrivateKey priv = keyPair.getPrivate();
        String algorithm = "SHA1withECDSA";
        byte[] signature = generateSignature(algorithm, priv, message);
        writeByte(signature);
        Certificate cert = null;
        try {
            cert = genCertificate(keyPair, algorithm, "selfSignedCert", 28);
        } catch (OperatorCreationException | CertIOException | CertificateException e) {
            e.printStackTrace();
        }
        Certificate[] chain = { cert };
        //ks.storePrivateKeyEntry(priv, "privateKey", chain);
        
        
        
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

    public void loadSavedGame(){
        getDialogService().showInputBox("Enter Saved Game's Name", savedName -> {
            String savedPath = savedName + ".sav";
            File saveFile = new File(savedName + ".sav");
            boolean saveExists = false;
            if (saveFile.exists()) {
                saveExists = true;
            }
            if (saveExists) {
                getSaveLoadService().readAndLoadTask(savedPath).run();
            } else {
                System.out.println("Save file not found");
            }
            System.out.println(savedPath);
        });
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
    
    private static X509Certificate genCertificate(KeyPair keyPair, String algo, String name, int days) throws OperatorCreationException,
            CertIOException, CertificateException {
        Instant now = Instant.now();
        Date before = Date.from(now);
        Date after = Date.from(now.plus(java.time.Duration.ofDays(days)));
        ContentSigner contentSigner = new JcaContentSignerBuilder(algo).build(keyPair.getPrivate());
        X500Name x500Name = new X500Name("CN=" + name);
        X509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(x500Name, BigInteger.valueOf(now.toEpochMilli()),
                                                                                before, after, x500Name, keyPair.getPublic())
                                                                                                           .addExtension(Extension.subjectKeyIdentifier, false, hashPublicKey(keyPair.getPublic()))
                                                                                                           .addExtension(Extension.authorityKeyIdentifier, false, hashAuthorityPublicKey(keyPair.getPublic()))
                                                                                                           .addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        return new JcaX509CertificateConverter().getCertificate(certBuilder.build(contentSigner));
    }
    
    private static SubjectKeyIdentifier hashPublicKey(PublicKey publicKey) throws OperatorCreationException, 
            CertIOException {
        SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        
        DigestCalculator digest = new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
        
        return new X509ExtensionUtils(digest).createSubjectKeyIdentifier(info);
    }
    
    private static AuthorityKeyIdentifier hashAuthorityPublicKey(PublicKey publicKey) throws OperatorCreationException {
        SubjectPublicKeyInfo info = SubjectPublicKeyInfo.getInstance(publicKey.getEncoded());
        DigestCalculator digest = new BcDigestCalculatorProvider().get(new AlgorithmIdentifier(OIWObjectIdentifiers.idSHA1));
        return new X509ExtensionUtils(digest).createAuthorityKeyIdentifier(info);
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
                = new FileOutputStream("src\\main\\resources\\PongApp.sig");
  
            // Starts writing the bytes in it
            os.write(bytes);
            System.out.println("Successfully"
                               + " byte inserted");
  
            // Close the file
            os.close();
        }
  
        catch (IOException e) {
            System.out.println("Exception: " + e);
        }
    }
    static byte[] getByteArrayFromFile(String filePath){
         byte[] byteArray = new byte[0];
        try { 
            byteArray = FileUtils.readFileToByteArray(new File(filePath));
            return  byteArray;
            
        }
        catch(IOException e){
            System.out.println(e.getMessage());
        }
        
        return  byteArray;
        
    }
    
    boolean verifySignature(byte[] signature, PublicKey publickey, String algorithm, String message) 
            throws NoSuchAlgorithmException, NoSuchProviderException, 
            InvalidKeyException, UnsupportedEncodingException, SignatureException {
        
        //Create an instance of the signature scheme for the given signature algorithm
        Signature sig = Signature.getInstance(algorithm, "SunEC");
        
        //Initialize the signature verification scheme.
        sig.initVerify(publickey);
        
        //Compute the signature.
        sig.update(message.getBytes("UTF-8"));
        
        //Verify the signature.
        boolean validSignature = sig.verify(signature);
        
        if(validSignature) {
            System.out.println("\nSignature is valid");
        } else {
            System.out.println("\nSignature is NOT valid!!!");
        }
        
        return validSignature;
    }
}
