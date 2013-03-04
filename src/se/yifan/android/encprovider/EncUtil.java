package se.yifan.android.encprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.util.Random;

/**
 * User: robert
 * Date: 03/03/13
 */
public class EncUtil {
    public static final String COLUMN_ENC_KEY = "encrypted_key";
    private static char[] password = {1, 2, 3, 4};
    private static Logger logger = LoggerFactory.getLogger(EncUtil.class);

    public static SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        long startTime = System.currentTimeMillis();
        /* Derive the key, given password and salt. */
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        Random r = new SecureRandom();
        byte[] salt = new byte[8];
        r.nextBytes(salt);
        KeySpec spec = new PBEKeySpec(password, salt, 65536 / 2, 128);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
        byte[] encoded = secret.getEncoded();
        String data = new BigInteger(1, encoded).toString(16);
        logger.info("encrypted_key: " + data);

        long endTime = System.currentTimeMillis();
        logger.info("Encryption took: " + (endTime - startTime));

        return secret;
    }

    public static byte[] encryptMsg(String message, SecretKey secret) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
    /* Encrypt the message. */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] ciphertext = cipher.doFinal(message.getBytes("UTF-8"));
        logger.info("encrypted: " + new String(ciphertext, "UTF-8"));
        return ciphertext;
    }

    public static void decryptMsg(byte[] ciphertext, SecretKey secret) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {
        /* Decrypt the message, given derived key and initialization vector. */
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        AlgorithmParameters params = cipher.getParameters();
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        String plaintext = new String(cipher.doFinal(ciphertext), "UTF-8");
        System.out.println(plaintext);
    }
}
