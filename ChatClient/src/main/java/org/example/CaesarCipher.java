package org.example;
import java.io.FileWriter;
import java.io.IOException;

public class CaesarCipher implements EncryptionMethod {

    @Override
    public String generateKey() {
        int randomNumber = (int) (Math.random() * 25) + 1;
        return String.valueOf(randomNumber);
    }

    @Override
    public String encrypt(String key, String input) {
        int keyNum = Integer.parseInt(key);
        String processedText = caesarCipher(input, keyNum);

        saveToFile(processedText, "encrypted.txt");

        return processedText;
    }

    @Override
    public String decrypt(String key, String input) {
        int keyNum = Integer.parseInt(key);
        String processedText = caesarCipher(input, -keyNum);

        saveToFile(processedText, "decrypted.txt");

        return processedText;
    }

    public String caesarCipher(String input, int key) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (Character.isLetter(c)) {
                char shiftedChar;
                if (Character.isLowerCase(c)) {
                    shiftedChar = (char) ('a' + (c - 'a' + key + 26) % 26);
                } else {
                    shiftedChar = (char) ('A' + (c - 'A' + key + 26) % 26);
                }
                result.append(shiftedChar);
            } else {
                result.append(c);
            }
        }

        return result.toString();
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
