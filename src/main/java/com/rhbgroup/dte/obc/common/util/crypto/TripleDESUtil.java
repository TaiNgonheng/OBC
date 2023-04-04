package com.rhbgroup.dte.obc.common.util.crypto;

import static java.lang.Math.min;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import javax.crypto.*;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("all")
public class TripleDESUtil {

  /**
   * Encrypts a string using the 3DES algorithm
   *
   * @param plainTextString String to encrypt
   * @param keyAsBytes Key to encrypt string with - 24 bytes
   * @return Encrypted string
   */
  private static final String ENCRYPTED_ALGO = "DESede/CBC/NoPadding";

  private static final String DECRYPTED_ALGO = "DESede/ECB/NoPadding";

  private static final String DES = "DESede";

  public static byte[] encrypt3Des(String plainTextString, byte[] keyAsBytes) {
    // Duplicate first 8 bytes
    if (keyAsBytes.length == 16) {
      byte[] tempKey = new byte[24];
      System.arraycopy(keyAsBytes, 0, tempKey, 0, 16);
      System.arraycopy(keyAsBytes, 0, tempKey, 16, 8);
      keyAsBytes = tempKey;
    }

    try {
      // Padding
      byte[] plainTextUnpadded = plainTextString.getBytes("UTF8");
      byte[] plainText = new byte[16];
      System.arraycopy(plainTextUnpadded, 0, plainText, 0, min(16, plainTextUnpadded.length));
      for (int i = plainTextUnpadded.length; i < plainText.length; i++) {
        plainText[i] = (byte) 255;
      }

      // Generate Cipher
      Cipher cipher = Cipher.getInstance(ENCRYPTED_ALGO);
      SecretKeySpec key = new SecretKeySpec(keyAsBytes, DES);
      IvParameterSpec ivspec = new IvParameterSpec(new byte[8]);

      // Encrypt
      cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
      return cipher.doFinal(plainText);
    } catch (NoSuchAlgorithmException
        | NoSuchPaddingException
        | BadPaddingException
        | UnsupportedEncodingException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | InvalidKeyException e) {
      return new byte[0];
    }
  }

  public static String convertBytesToHexString(byte[] bytes) {
    char[] hexArray = "0123456789ABCDEF".toCharArray();
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static byte[] convertHexStringToBytes(String hex) {
    hex = hex.replace(" ", "");
    int len = hex.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] =
          (byte)
              ((Character.digit(hex.charAt(i), 16) << 4) + Character.digit(hex.charAt(i + 1), 16));
    }
    return data;
  }

  public static byte[] decrypt(String plainTextHex, String keyHex) throws Exception {
    SecureRandom random = new SecureRandom();
    DESedeKeySpec deSedeKeySpec = new DESedeKeySpec(convertHexStringToBytes(keyHex));
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(DES);
    SecretKey secretKey = keyFactory.generateSecret(deSedeKeySpec);

    byte[] inputBytes = convertHexStringToBytes(plainTextHex);
    Cipher cipher = Cipher.getInstance(DECRYPTED_ALGO);
    cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
    return cipher.doFinal(inputBytes);
  }

  /**
   * Encrypts a string using the 3DES algorithm
   *
   * @param plainTextHex Hex String to encrypt
   * @param keyAsBytes Key to encrypt string with - 24 bytes
   * @return Encrypted string
   */
  public static byte[] encrypt3DesHex(String plainTextHex, byte[] keyAsBytes) {
    // Duplicate first 8 bytes
    if (keyAsBytes.length == 16) {
      byte[] tempKey = new byte[24];
      System.arraycopy(keyAsBytes, 0, tempKey, 0, 16);
      System.arraycopy(keyAsBytes, 0, tempKey, 16, 8);
      keyAsBytes = tempKey;
    }

    try {
      // Padding
      byte[] plainTextUnpadded = TripleDESUtil.convertHexStringToBytes(plainTextHex);
      byte[] plainText = new byte[16];
      System.arraycopy(plainTextUnpadded, 0, plainText, 0, min(16, plainTextUnpadded.length));
      for (int i = plainTextUnpadded.length; i < plainText.length; i++) {
        plainText[i] = (byte) 255;
      }

      // Generate Cipher
      Cipher cipher = Cipher.getInstance(ENCRYPTED_ALGO);
      SecretKeySpec key = new SecretKeySpec(keyAsBytes, DES);
      IvParameterSpec ivspec = new IvParameterSpec(new byte[8]);

      // Encrypt
      cipher.init(Cipher.ENCRYPT_MODE, key, ivspec);
      return cipher.doFinal(plainText);
    } catch (NoSuchAlgorithmException
        | NoSuchPaddingException
        | BadPaddingException
        | InvalidAlgorithmParameterException
        | IllegalBlockSizeException
        | InvalidKeyException e) {
      return new byte[0];
    }
  }
}
