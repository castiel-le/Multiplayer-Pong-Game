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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Class for cryptographic operations
 *
 * @author Alex
 */
public class CryptoUtility {

    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;
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
        try {
            // Create keystore
            this.ksHashedPassword = computeHash(password.toString());
            ks = KeyStore.getInstance(KeyStore.getDefaultType());
            ks.load(null, computeHash(this.ksHashedPassword.toString()));
            passProtection = new KeyStore.PasswordProtection(computeHash(password.toString()));
            // Store keystore
            FileOutputStream fos = new FileOutputStream("keystore.p12");
            //ks.store(fos, password);
            System.out.println("KeyStore stored");
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        
    }
    
    
    
    public KeyStore getKeyStore() {
        return this.ks;
    }
    
    public KeyStore.Entry getKeyStoreEntry(String alias) throws NoSuchAlgorithmException,
                                UnrecoverableEntryException, KeyStoreException {
        return ks.getEntry(alias, this.passProtection);
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
     * 
     * @param alias "selfSignedCert"
     * @return 
     */
    public PublicKey getPublicKey(String alias) {
        PublicKey returnedKey = null;
        try {
            PrivateKey key = (PrivateKey) ks.getKey(alias, ksHashedPassword);
            Certificate cert = ks.getCertificate(alias);
            returnedKey = cert.getPublicKey();
        } catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }
        return returnedKey;
    }
    
    /**
     * SHA3-256 String hashing.
     *
     * @param message
     * @return  char[] of hash
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
        //Charset charset = Charset.forName("UTF-8");
        //String temp = new String(bytes, charset);
        String temp = Base64.getEncoder().encodeToString(bytes);
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
     * Stores a secret key entry in the KeyStore
     * User should be asked to verify KeyStore password in this operation
     * @param secretKey
     * @param alias 
     * @return boolean  Returns whether the KeyStore Password is valid.
     */
    public boolean storeSecretKeyEntry(SecretKey secretKey, String alias) {
        KeyStore.SecretKeyEntry secretKeyEntry = new KeyStore.SecretKeyEntry(secretKey);
        try {
            ks.load(new FileInputStream("src\\main\\resources\\keystore.p12"), this.ksHashedPassword);
            ks.setEntry(alias, secretKeyEntry, passProtection);
            System.out.println("Secret key entry stored in the KeyStore.");
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException 
                                   | CertificateException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    /**
     * Stores a secret key entry in the KeyStore
     * User should have been asked to verify KeyStore password in this operation
     * @param secretKey
     * @param alias 
     * @return boolean  Returns whether the KeyStore Password is valid.
     */
    public boolean storePrivateKeyEntry(PrivateKey privateKey, String alias, Certificate[] chain) {
        var privateKeyEntry = new KeyStore.PrivateKeyEntry(privateKey, chain);
        try {
            ks.load(new FileInputStream("src\\main\\resources\\keystore.p12"), this.ksHashedPassword);
            ks.setEntry(alias, privateKeyEntry, passProtection);
            System.out.println("Secret key entry stored in the KeyStore.");
        } catch (KeyStoreException | IOException | NoSuchAlgorithmException 
                                   | CertificateException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }    

    /**
     * Method for generating 12 byte GCM Initialization Vector. 
     * @return 
     */
    byte[] generateGCMIV() {
        byte[] GCMIV = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(GCMIV);
        return GCMIV;
    }
    
    /**
     * /**
     * Method to encrypt a file.
     * @param algorithm Encryption algorithm type
     * @param key       Secret key
     * @param IV        Vector
     * @param inputFile
     * @param outputFile
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    void encryptFile(String algorithm, SecretKey key, byte[] IV, File inputFile, File outputFile)
            throws NoSuchAlgorithmException, BadPaddingException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            FileNotFoundException, IOException {

        //Create an instance of the Cipher class
        Cipher cipher = Cipher.getInstance(algorithm);

        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

        cipher.init(Cipher.ENCRYPT_MODE, key, gcmParameterSpec);

        FileOutputStream outputStream;
        //Create output stream
        try (
                FileInputStream inputStream = new FileInputStream(inputFile)) {
            //Create output stream
            outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[64];
            int bytesRead;
            //Read up to 64 bytes of data at a time
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                //Cipher.update method takes byte array, input offset and input lentth
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    //Write the ciphertext for the buffer to the output file
                    outputStream.write(output);
                }
            }   //Encrypt the last buffer of plaintext 
            byte[] output = cipher.doFinal();
            if (output != null) {
                outputStream.write(output);
            }
            //Close the input and output streams
        }
        outputStream.close();
    }

    
    /**
     * Method to decrypt a file.
     * @param algorithm Encryption algorithm type
     * @param key       Secret key
     * @param IV        Vector
     * @param inputFile
     * @param outputFile
     * @throws NoSuchAlgorithmException
     * @throws BadPaddingException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    void decryptFile(String algorithm, SecretKey key,
            byte[] IV, File inputFile,
            File outputFile)
            throws NoSuchAlgorithmException, BadPaddingException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, IllegalBlockSizeException,
            FileNotFoundException, IOException {

        //Create an instance of the Cipher class
        Cipher cipher = Cipher.getInstance(algorithm);

        // Create GCMParameterSpec
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, IV);

        cipher.init(Cipher.DECRYPT_MODE, key, gcmParameterSpec);

        FileOutputStream outputStream;
        //Create output stream
        try (
                FileInputStream inputStream = new FileInputStream(inputFile)) {
            //Create output stream
            outputStream = new FileOutputStream(outputFile);
            byte[] buffer = new byte[64];
            int bytesRead;
            //Read up to 64 bytes of data at a time
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byte[] output = cipher.update(buffer, 0, bytesRead);
                if (output != null) {
                    //Write the Plaintext to the output file
                    outputStream.write(output);
                }
            }   //Decrypt the last buffer of ciphertext
            byte[] output = cipher.doFinal();
            if (output != null) {
                outputStream.write(output);
            }
            //Close the input and output streams.
        }
        outputStream.close();
    }
    
    /**
     * Method to store the KeyStore using an instance of this class
     * @throws IOException 
     */
    public void storeKeyStore() throws IOException {
        try {
            this.ks.store(new FileOutputStream(".\\src\\main\\resources\\keystore.p12"), ksHashedPassword);
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException e) {
            e.printStackTrace();
        }
    }
}
