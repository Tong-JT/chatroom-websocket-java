package org.example;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

public class AESCrypto implements EncryptionMethod {

    public String generateKey() {
        KeyGenerator keyGenerator = null;
        try {
            keyGenerator = KeyGenerator.getInstance("AES");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public String encrypt(String key, String plainText){
        try {
            String processedText = encryptOrDecrypt(key, Cipher.ENCRYPT_MODE, plainText);
            saveToFile(processedText, "encrypted.txt");
            return processedText;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String key, String cipherText){
        try {
            String processedText = encryptOrDecrypt(key, Cipher.DECRYPT_MODE, cipherText);
            saveToFile(processedText, "decrypted.txt");
            return processedText;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private String encryptOrDecrypt(String key, int mode, String inputText) throws Throwable {
        byte[] decodedKey = Base64.getDecoder().decode(key);
        SecretKeySpec aesKey = new SecretKeySpec(decodedKey, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(mode, aesKey);
        byte[] inputBytes = inputText.getBytes(StandardCharsets.UTF_8);
        byte[] outputBytes = cipher.doFinal(inputBytes);

        return Base64.getEncoder().encodeToString(outputBytes);
    }

    public void saveToFile(String text, String filename) {
        try (FileWriter writer = new FileWriter(filename, true)) {
            writer.write(text);
            writer.write(System.lineSeparator());
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }
}
