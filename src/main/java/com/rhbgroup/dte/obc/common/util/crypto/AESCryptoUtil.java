package com.rhbgroup.dte.obc.common.util.crypto;

import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESCryptoUtil {

  private AESCryptoUtil() {}

  private static final String ALGORITHM = "AES/CBC/NoPadding";
  private static final String KEY_ALGORITHM = "AES";

  public static byte[] encrypt(String data, String secretKey, byte[] iv) {

    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
      return cipher.doFinal(
          CryptoPadding.pad(data.getBytes(StandardCharsets.UTF_8), cipher.getBlockSize()));

    } catch (Exception ex) {
      return new byte[0];
    }
  }

  public static byte[] decrypt(byte[] data, String secretKey, byte[] iv) {
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);

    SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, KEY_ALGORITHM);
    IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
      byte[] decryptedBytes = cipher.doFinal(data);

      return CryptoPadding.unPad(decryptedBytes);

    } catch (Exception ex) {
      return new byte[0];
    }
  }

  public static byte[] getRandomIv(int blockSize) {
    SecureRandom random = new SecureRandom();
    byte[] iv = new byte[blockSize];
    random.nextBytes(iv);

    return iv;
  }

  public static void main(String[] args) throws NoSuchPaddingException, NoSuchAlgorithmException {
    Cipher cipher = Cipher.getInstance(ALGORITHM);
    byte[] randomIv = getRandomIv(cipher.getBlockSize());

    byte[] encode = Base64.getEncoder().encode(randomIv);
    String s = new String(encode, StandardCharsets.UTF_8);

    System.out.println("RANDOM IV >> " + s);

    byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
    byte[] decode = Base64.getDecoder().decode(bytes);

    byte[] encrypt = encrypt("sorrynext@1", "1234567812345678", decode);

    String s2 = new String(Base64.getEncoder().encode(encrypt), StandardCharsets.UTF_8);

    System.out.println("ENCRYPTED STRING >> " + s2);

    byte[] decrypt =
        decrypt(
            Base64.getDecoder().decode(s2.getBytes(StandardCharsets.UTF_8)),
            "1234567812345678",
            decode);

    String s1 = new String(decrypt, StandardCharsets.UTF_8);

    System.out.println("DECRYPTED STRING >> " + s1);
  }
}
