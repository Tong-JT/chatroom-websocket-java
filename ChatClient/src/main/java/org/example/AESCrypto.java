package org.example;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
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

    public String encrypt(String key, String plainText) {
        try {
            String processedText = encryptOrDecrypt(key, Cipher.ENCRYPT_MODE, plainText);
            saveToFile(processedText, "encrypted.txt");
            return processedText;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public String decrypt(String key, String cipherText) {
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
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[16];
        random.nextBytes(iv);

        if (mode == Cipher.ENCRYPT_MODE) {
            cipher.init(mode, aesKey, new IvParameterSpec(iv));
            byte[] inputBytes = inputText.getBytes(StandardCharsets.UTF_8);
            byte[] encryptedBytes = cipher.doFinal(inputBytes);

            byte[] combined = new byte[iv.length + encryptedBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encryptedBytes, 0, combined, iv.length, encryptedBytes.length);

            return Base64.getEncoder().encodeToString(combined);
        } else {
            byte[] decodedCiphertext = Base64.getDecoder().decode(inputText);
            byte[] ivExtracted = new byte[16];
            System.arraycopy(decodedCiphertext, 0, ivExtracted, 0, ivExtracted.length);

            IvParameterSpec ivSpec = new IvParameterSpec(ivExtracted);
            byte[] cipherTextBytes = new byte[decodedCiphertext.length - ivExtracted.length];
            System.arraycopy(decodedCiphertext, ivExtracted.length, cipherTextBytes, 0, cipherTextBytes.length);

            cipher.init(mode, aesKey, ivSpec);
            byte[] decryptedBytes = cipher.doFinal(cipherTextBytes);

            return new String(decryptedBytes, StandardCharsets.UTF_8);
        }
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
