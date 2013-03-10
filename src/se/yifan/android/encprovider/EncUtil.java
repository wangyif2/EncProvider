package se.yifan.android.encprovider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;

/**
 * User: robert
 * Date: 03/03/13
 */
public class EncUtil {
    public static final String COLUMN_ENC_KEY = "encrypted_key";
    private static String password = "tvnw63ufg9gh5392";
    private static Logger logger = LoggerFactory.getLogger(EncUtil.class);

    public static SecretKey generateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        long startTime = System.currentTimeMillis();
        /* Derive the encContentValues, given password and salt. */
//        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
//        Random r = new SecureRandom();
//        byte[] salt = new byte[8];
//        r.nextBytes(salt);
//        KeySpec spec = new PBEKeySpec(password, salt, 65536 / 2, 128);
//        SecretKey tmp = factory.generateSecret(spec);
//        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");
//        byte[] encoded = secret.getEncoded();
//        String data = new BigInteger(1, encoded).toString(16);

        SecretKey secret = new SecretKeySpec(password.getBytes(), "AES");
        byte[] encoded = secret.getEncoded();
        String data = new BigInteger(1, encoded).toString(16);
        logger.info("encrypted_key: " + data);

        long endTime = System.currentTimeMillis();
        logger.info("Encryption took: " + (endTime - startTime));

        return secret;
    }

    public static byte[] encryptMsg(String message, SecretKey secret) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException {
    /* Encrypt the message. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        byte[] cipherText = cipher.doFinal(message.getBytes("UTF-8"));
        logger.info("encrypted: " + new String(cipherText, "UTF-8"));
        return cipherText;
    }

    public static String decryptMsg(byte[] cipherText, SecretKey secret) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidParameterSpecException, InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException, UnsupportedEncodingException {

        /* Decrypt the message, given derived encContentValues and initialization vector. */
        Cipher cipher = null;
        cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        //        AlgorithmParameters params = cipher.getParameters();
//        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
//        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        cipher.init(Cipher.DECRYPT_MODE, secret);
        return new String(cipher.doFinal(cipherText), "UTF-8");
    }

//    public static byte[] keyToString(SecretKey secretKey) {
//        byte[] encoded = secretKey.getEncoded();
//        return new BigInteger(1, encoded).toString(16);
//        return Base64.encodeToString(secretKey.getEncoded(), Base64.DEFAULT);
//    }

//    public static SecretKey stringToKey(String secretKey) {
//        byte[] encoded = new BigInteger(secretKey, 16).toByteArray();
//        return new SecretKeySpec(encoded, "AES");
//        byte[] encodedKey     = Base64.decode(secretKey, Base64.DEFAULT);
//        return new SecretKeySpec(encodedKey, 0, encodedKey.length, "AES");
//    }
}