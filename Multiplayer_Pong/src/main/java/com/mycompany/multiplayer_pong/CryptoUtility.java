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
import java.nio.charset.Charset;
import java.security.cert.CertificateException;
import java.util.Arrays;
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
    private char[] ksHashedPassword;

    /**
     *  Constructor for creating a new KeyStore file with given password. 
     * @param password
     * @throws IOException
     */
    public CryptoUtility(char[] password) throws IOException {
        //Ensure the use of "JavaFX Password Field UI control" or something similar
        //Ensure string normalization
        
        FileInputStream fis = null;
        try {
            if (checkKeyStoreExists()) {
                fis = new FileInputStream("src\\main\\resources\\keystore.p12");
            }
            // Computing hash and storing it in a char[]
            String tempPass = new String(computeHash(password.toString()));
            ksHashedPassword = computeHash(tempPass);
            
            ks.load(fis, ksHashedPassword);
            System.out.println("KeyStore file created at \"src\\main\\resources\\keystore.p12\"");
        } catch (NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        } finally {
            fis.close();
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("src\\main\\resources\\keystore.p12");
            ks.store(fos, password);
        } catch (IOException | KeyStoreException | NoSuchAlgorithmException 
                                                 | CertificateException e) {
            e.printStackTrace();
        } finally {
            fos.close();
        }
    }
    
    /**
     * Checks if a keystore.p12 file exists
     * @return 
     */
    public boolean checkKeyStoreExists() {
        return new File("src\\main\\resources\\keystore.p12").exists();
    }
    
    /**
     * To prompt the user when access to KeyStore is needed
     * @param passwordInput
     * @return 
     */
    public boolean verifyKSPassword(String passwordInput) {
        char[] tempPassInput = computeHash(passwordInput);
        return Arrays.equals(tempPassInput, ksHashedPassword);
        
    }
    
    /**
     * SHA3-256 String hashing.
     *
     * @param message
     * @return
     */
    public char[] computeHash(String message) {
        char[] hash = null;
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA3-256");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        byte[] bytes = messageDigest.digest(message.getBytes(StandardCharsets.UTF_8));
        Charset charset = Charset.forName("UTF-8");
        String temp = new String(bytes, charset);
        hash = temp.toCharArray();
        return hash;
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
    /*public void storeKeyStore() throws IOException {
        if (new File("src\\main\\resources\\keystore.p12").exists()) {
            try {
                FileOutputStream fsOutput = new FileOutputStream("src\\main\\resources\\keystore.p12");

                ks.store(fsOutput, ksHashedPassword);
                System.out.println("Key stored");
            } catch (KeyStoreException | FileNotFoundException | NoSuchAlgorithmException
                                       | CertificateException e) {
                e.printStackTrace();
            }
        }
    }*/
    
    /**
     * Stores a secret key entry in the KeyStore
     * User should be asked to verify KeyStore password in this operation
     * @param secretKey
     * @param alias 
     */
    public void storeSecretKeyEntry(SecretKey secretKey, String alias) {
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        try {
            ks.setEntry(alias, secretKeyEntry, passProtection);
            System.out.println("Secret key entry stored in the KeyStore.");
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }
}
