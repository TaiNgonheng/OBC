package com.rhbgroup.dte.obc.common.util.crypto;

import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

@SuppressWarnings("all")
public class TripleDESCryptoUtil extends CryptoUtil {

  private TripleDESCryptoUtil() {}

  private static final String ALGORITHM_CBC = "DESede/CBC/NoPadding";
  private static final String ALGORITHM_ECB = "DESede/ECB/NoPadding";
  private static final String KEY_ALGORITHM = "DESede";

  public static byte[] encrypt(byte[] data, byte[] key, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_CBC);
      cipher.init(
          Cipher.ENCRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), new IvParameterSpec(iv));

      return cipher.doFinal(data);

    } catch (Exception ex) {
      return new byte[0];
    }
  }

  public static byte[] decrypt(byte[] data, byte[] key, byte[] iv) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_CBC);
      cipher.init(
          Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), new IvParameterSpec(iv));

      return cipher.doFinal(data);

    } catch (Exception ex) {
      return new byte[0];
    }
  }

  public static byte[] decryptECB(byte[] data, byte[] key) {
    try {
      Cipher cipher = Cipher.getInstance(ALGORITHM_ECB);
      cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, KEY_ALGORITHM), new SecureRandom());

      return cipher.doFinal(data);

    } catch (Exception ex) {
      return new byte[0];
    }
  }
}
