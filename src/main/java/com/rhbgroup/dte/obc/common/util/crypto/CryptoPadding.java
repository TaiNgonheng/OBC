package com.rhbgroup.dte.obc.common.util.crypto;

public class CryptoPadding {

  private CryptoPadding() {}

  // Padding method for PKCS5Padding
  public static byte[] pad(byte[] data, int blockSize) {
    int paddingLength = blockSize - (data.length % blockSize);
    byte[] paddedData = new byte[data.length + paddingLength];
    System.arraycopy(data, 0, paddedData, 0, data.length);
    for (int i = 0; i < paddingLength; i++) {
      paddedData[data.length + i] = (byte) paddingLength;
    }
    return paddedData;
  }

  // UnPadding method for PKCS5Padding
  public static byte[] unPad(byte[] paddedData) {
    int paddingLength = paddedData[paddedData.length - 1];
    byte[] unPaddedData = new byte[paddedData.length - paddingLength];
    System.arraycopy(paddedData, 0, unPaddedData, 0, unPaddedData.length);
    return unPaddedData;
  }
}
