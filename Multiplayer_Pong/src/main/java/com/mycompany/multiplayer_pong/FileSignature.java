package com.mycompany.multiplayer_pong;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;

import static com.almasb.fxgl.dsl.FXGL.getDialogService;

public class FileSignature {
    private static final String ALGORITHM = "SHA256withECDSA";
    private static final String SIGNATUREPATH = "src/main/resources/assets/MultiplayerPong.sig";

    //Method for generating digital signature.
    public static void generateSignature(PrivateKey privatekey, String pongPath) throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidKeyException, IOException, SignatureException {


        //Create an instance of the signature scheme for the given signature algorithm
        Signature sig = Signature.getInstance(ALGORITHM, "SunEC");

        //Initialize the signature scheme
        sig.initSign(privatekey);
        Path path = Paths.get(pongPath);
        Path signaturePath = Paths.get(SIGNATUREPATH);
        //Compute the signature
        String message = new String(Files.readAllBytes(path));
        sig.update(message.getBytes("UTF-8"));
        byte[] signature = sig.sign();

        Files.write(signaturePath, signature);

    }

    //Method for verifying digital signature.
    public static boolean verifySignature(PublicKey publickey, String pongPath)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidKeyException, IOException, SignatureException {

        //Create an instance of the signature scheme for the given signature algorithm
        Signature sig = Signature.getInstance(ALGORITHM, "SunEC");

        //Initialize the signature verification scheme.
        sig.initVerify(publickey);
        Path path = Paths.get(pongPath);

        String message = new String(Files.readAllBytes(path));
        //Compute the signature.
        sig.update(message.getBytes("UTF-8"));

        Path signaturePath = Paths.get(SIGNATUREPATH);
        //Verify the signature.
        boolean validSignature = sig.verify(Files.readAllBytes(signaturePath));

        if (validSignature) {
            getDialogService().showMessageBox("Signature is valid");
        } else {
            getDialogService().showMessageBox("Signature is NOT valid!!!");
        }

        return validSignature;
    }
}
