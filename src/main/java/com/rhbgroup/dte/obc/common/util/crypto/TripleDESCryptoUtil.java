package com.rhbgroup.dte.obc.common.util.crypto;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class TripleDESCryptoUtil extends CryptoUtil {

  private TripleDESCryptoUtil() {
    super();
  }

  private static final String ALGORITHM = "DESede/CBC/NoPadding";
  private static final String KEY_ALGORITHM = "DESede";

  public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(
          Cipher.ENCRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), new IvParameterSpec(iv));

      return cipher.doFinal(data);

    } catch (Exception ex) {
      return new byte[0];
    }
  }

  public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM);
      cipher.init(
          Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), new IvParameterSpec(iv));
      byte[] encryptedBytes = cipher.doFinal(data);
      return encryptedBytes;

    } catch (Exception ex) {
      return new byte[0];
    }
  }
}
