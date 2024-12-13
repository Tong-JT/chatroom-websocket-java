package org.example;

public interface EncryptionMethod {
    public String generateKey();
    public String encrypt(String key, String input);
    public String decrypt(String key, String input);
}
