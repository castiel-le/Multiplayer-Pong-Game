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
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class for cryptographic operations
 *
 * @author Alex
 */
public class CustomKeyStore {

    private KeyStore ks;
    private KeyStore.PasswordProtection passProtection;
    private char[] ksPassword;
    private FileInputStream fsInput;

    /**
     *  Constructor. 
     * @param password
     * @throws IOException
     */
    public CustomKeyStore(char[] password) throws IOException {
        //Ensure the use of "JavaFX Password Field UI control" or something similar

        fsInput = null;
        try {
            ks = KeyStore.getInstance("PKCS12");
            fsInput = new FileInputStream("src\\main\\resources\\keystore.p12");
            ks.load(fsInput, password);
        } catch (CertificateException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        this.passProtection = new KeyStore.PasswordProtection(password);
        closeFSInput();
    }
    
    /**
     * SHA3-256 String hashing
     *
     * @param message
     * @param algo
     * @return
     */
    public byte[] computeHash(String message, String algo) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance(algo);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return messageDigest.digest(message.getBytes(StandardCharsets.UTF_8));
    }
    
    public void encryptFile(String algo, SecretKey secretKey, )
    
    /**
     * Creates secret key of type ECDSA with password
     * @param keyPassword
     * @return 
     */
    public SecretKey generateSecretKeyWithPassword(String keyPassword) {
        
        return new SecretKeySpec(keyPassword.getBytes(), "ECDSA");
    }
    
    /**
     * Generates secret key using AES
     * @param n should be a value of 128, 192 or 256
     * @return 
     */
    public SecretKey generateSecretKey(int n) {
        // n 
        KeyGenerator keyGen = null;
        try {
            keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(n);
        
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keyGen.generateKey();
    }
    
    /**
     * Stores KeyStore object in "src\main\resources\keystore.p12"
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
    
    public void storeEntry(SecretKey secretKey, String alias) {
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        try {
            ks.setEntry(alias, secretKeyEntry, passProtection);
            System.out.println("Secret key entry stored in the KeyStore.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Releases resources used by FileInputStream by closing it
     * Helper method
     */
    private void closeFSInput() {
        try {
            fsInput.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
