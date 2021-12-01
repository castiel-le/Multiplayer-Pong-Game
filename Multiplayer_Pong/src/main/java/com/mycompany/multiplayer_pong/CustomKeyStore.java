package com.mycompany.multiplayer_pong;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertificateException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class for cryptographic operations
 *
 * @author Alex
 */
public class CustomKeyStore {

    private KeyStore ks;
    private SecretKey secretKey;
    private KeyStore.PasswordProtection passProtection;
    private char[] ksPassword;

    /**
     *  Constructor
     * @param password
     * @throws IOException
     */
    public CustomKeyStore(char[] password) throws IOException {
        //Ensure the use of "JavaFX Password Field UI control" or something similar

        FileInputStream fsInput = null;
        try {
            ks = KeyStore.getInstance("PKCS12");
            fsInput = new FileInputStream("src\\main\\resources\\keystore.p12");
            ks.load(fsInput, password);
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        this.passProtection = new KeyStore.PasswordProtection(password);
    }
    
    /**
     * SHA3-256 String hashing
     *
     * @param message
     * @param algo
     * @return
     */
    public byte[] computeHash(String message, String algo) throws NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance(algo);

        return messageDigest.digest(message.getBytes(StandardCharsets.UTF_8));
    }
    
    /**
     * 
     * @param keyPassword
     * @return 
     */
    public SecretKey createSecretKey(String keyPassword) {
        
        this.secretKey = new SecretKeySpec(keyPassword.getBytes(), "ECDSA");
        return this.secretKey;
    }
    
    /**
     * 
     * @throws IOException 
     */
    public void storeKeyStore() throws IOException {
        try {
            FileOutputStream fsOutput = new FileOutputStream("src\\main\\resources\\keystore.p12");
        
            ks.store(fsOutput, ksPassword);
            System.out.println("Key stored");
        } catch (KeyStoreException | FileNotFoundException | NoSuchAlgorithmException
                                   | CertificateException e) {
            e.printStackTrace();
        }
    }
}
