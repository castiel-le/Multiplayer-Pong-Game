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
public class CryptoUtility {

    private KeyStore ks;
    private KeyStore.PasswordProtection passProtection;
    private char[] ksPassword;

    /**
     *  Constructor for creating a new KeyStore file. 
     * @param password
     * @throws IOException
     */
    public CryptoUtility(char[] password) throws IOException {
        //Ensure the use of "JavaFX Password Field UI control" or something similar
        FileInputStream fis = null;
        try {
            if (new File("src\\main\\resources\\keystore.p12").exists()) {
                fis = new FileInputStream("src\\main\\resources\\keystore.p12");
            }
            ks.load(fis, password);
        } catch (NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("src\\main\\resources\\keystore.p12");
            ks.store(fos, password);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException 
                                                 | CertificateException e) {
            e.printStackTrace();
        }
    }
    
    private boolean checkKeyStore() {
        return new File("src\\main\\resources\\keystore.p12").exists();
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
    
   // public void encryptFile(String algo, SecretKey secretKey, )
  
    
    /**
     * Generates secret key using AES
     * @param n size should be a value of 128, 192 or 256
     * @return 
     */
    public SecretKey generateSecretKey(int n) {
        // n 
        KeyGenerator keyGen = null;
        if (n == 128 || n == 192 || n == 256) {
            
        }
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
        if (new File("src\\main\\resources\\keystore.p12").exists()) {
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
    
    /**
     * Stores an entry in 
     * @param secretKey
     * @param alias 
     */
    public void storeEntry(SecretKey secretKey, String alias) {
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        try {
            ks.setEntry(alias, secretKeyEntry, passProtection);
            System.out.println("Secret key entry stored in the KeyStore.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }
}
