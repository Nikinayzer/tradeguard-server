package korn03.tradeguardserver.service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import org.springframework.stereotype.Service;

@Service
public class EncryptionService {

    private static final String SECRET_KEY = "5jS6N+5bisVPh75V3wV77ZUZCcYVD/oftiB4dMxdHtHw+gw1269rwKEdfVgpr4rt";  // Should be 16/24/32 characters
    private static final String ALGORITHM = "AES";

    private SecretKeySpec getKeySpec() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
    }

    /**
     * Encrypts a string using AES.
     */
    public String encrypt(String data) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, getKeySpec());
            byte[] encrypted = cipher.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("Encryption failed", e);
        }
    }

    /**
     * Decrypts an AES-encrypted string.
     */
    public String decrypt(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, getKeySpec());
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            return new String(cipher.doFinal(decoded));
        } catch (Exception e) {
            throw new RuntimeException("Decryption failed", e);
        }
    }
}
