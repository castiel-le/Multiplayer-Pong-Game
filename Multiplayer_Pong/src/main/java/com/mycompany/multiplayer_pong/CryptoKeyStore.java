package com.mycompany.multiplayer_pong;

import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Base64;
import java.security.KeyStore.*;

public class CryptoKeyStore {

    public static final int GCM_IV_LENGTH = 12;
    public static final int GCM_TAG_LENGTH = 16;


    private KeyStore ks;

    private char[] hashString;

    private final String KEYSTORE_PATH = "src/main/resources/assets/keystore/keystore.p12";

    private final String IV_PATH = "src/main/resources/assets/keystore/gcm.iv";

    private final String ALGORITHM = "AES/GCM/NoPadding";

    String[] cmd = {
            "keytool",
            "-genkeypair",
            "-noprompt",
            "-alias",
            "KEY_PAIR",
            "-dname",
            "cn=username",
            "-keyalg", "EC",
            "-groupname",
            "secp256r1",
            "-validity",
            "365",
            "-storetype",
            "PKCS12",
            "-keystore",
            KEYSTORE_PATH,
            "-storepass",
            ""
    };
    public CryptoKeyStore(String password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        this.hashString = computeHash("SHA3-256", password);
        cmd[cmd.length-1] = String.valueOf(this.hashString);
        ks = KeyStore.getInstance("PKCS12");
        checkKeyStore(KEYSTORE_PATH);
    }

    public void createKeyStore() throws IOException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        var start = pb.start();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(start.getInputStream()))){
            while (br.readLine() != null){
            }
        }
    }

    public void checkKeyStore(String KEYSTORE_PATH
    ) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        File keyFile = new File(KEYSTORE_PATH);
        if (!keyFile.exists()) {
            try {
                createKeyStore();
                loadKeyStore();
                storeSecretKey(generateKey(256));
                saveKeyStore();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void loadKeyStore() throws IOException, CertificateException, NoSuchAlgorithmException {
        ks.load(new FileInputStream(KEYSTORE_PATH), this.hashString);
    }

    public char[] computeHash(String algorithm, String password)throws NoSuchAlgorithmException{
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            byte[] hashbytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            String strpwd = Base64.getEncoder().encodeToString(hashbytes);
            return strpwd.toCharArray();
    }

    /**
     * *
     * Method for generating secret key takes an integer n; n can be 128, 192 or
     * 256.
     */
    SecretKey generateKey(int n) throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(n); //Initialize the key generator
        SecretKey key = keyGenerator.generateKey(); //Generate the key
        return key;
    }

    //Method for generating 12 byte GCM Initialization Vector.
    byte[] generateGCMIV() {
        byte[] GCMIV = new byte[GCM_IV_LENGTH];
        SecureRandom random = new SecureRandom();
        random.nextBytes(GCMIV);
        return GCMIV;
    }

    // Method to encrypt a file
    public void encryptFile(String algorithm, SecretKey key, byte[] IV, File inputFile, File outputFile)
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

    //Method to decrypt a file
    public void decryptFile(String algorithm, SecretKey key,
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

    public void storeSecretKey(SecretKey sk) throws CertificateException, IOException, NoSuchAlgorithmException, KeyStoreException {
        ProtectionParameter pp = new PasswordProtection(this.hashString);

        SecretKeyEntry ske = new SecretKeyEntry(sk);

        ks.setEntry("SECRET_KEY", ske, pp);
    }

    public void saveKeyStore() throws IOException, CertificateException, KeyStoreException, NoSuchAlgorithmException {
        ks.store(new FileOutputStream(KEYSTORE_PATH), this.hashString);
    }

    public SecretKey getSecretKey() throws CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        loadKeyStore();
        SecretKey sk = (SecretKey) ks.getKey("SECRET_KEY", this.hashString);
        return sk;
    }

    public PrivateKey getPrivateKey() throws CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        loadKeyStore();
        PrivateKey prk = (PrivateKey)  ks.getKey("KEY_PAIR", this.hashString);
        return prk;
    }

    public PublicKey getPublicKey() throws CertificateException, IOException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException {
        loadKeyStore();
        Certificate cert = ks.getCertificate("KEY_PAIR");
        return cert.getPublicKey();
    }

//    public static void main(String[] args) throws CertificateException, KeyStoreException, IOException, NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, UnrecoverableKeyException {
//        var keystore = new CryptoKeyStore("yikes");
//        System.out.println(keystore.getSecretKey().toString());
//        System.out.println(keystore.getPrivateKey().toString());
//        System.out.println(keystore.getPublicKey().toString());
//        var seckey = keystore.generateKey(256);
//        var IV = keystore.generateGCMIV();
//        File inputF = new File("wtf.sav");
//        File outputF = new File("wtf.sav.enc");
//        keystore.encryptFile(keystore.ALGORITHM, seckey, IV, inputF, outputF);
//        Files.write(Paths.get(keystore.IV_PATH), IV);
//        Files.deleteIfExists(Paths.get("wtf.sav"));
//    }
}
