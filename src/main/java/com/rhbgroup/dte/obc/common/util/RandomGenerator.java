package com.rhbgroup.dte.obc.common.util;

import java.security.SecureRandom;
import java.util.Random;

public class RandomGenerator {

  private static final String UPPERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWERS = UPPERS.toLowerCase();
  private static final String DIGITS = "0123456789";
  private static final String ALPHA_NUMERIC = UPPERS.concat(LOWERS).concat(DIGITS);

  private final int length;
  private final Random random;

  private RandomGenerator() {
    this.length = 32;
    this.random = new SecureRandom();
  }

  private RandomGenerator(int length, Random random) {
    this.length = length;
    this.random = random;
  }

  public static RandomGenerator getRandom(int length, Random random) {
    return new RandomGenerator(length, random);
  }

  public static RandomGenerator getDefaultRandom() {
    return new RandomGenerator();
  }

  public String nextString() {
    char[] alphaNumerics = ALPHA_NUMERIC.toCharArray();
    char[] randomChars = new char[length];
    for (int i = 0; i < length; i++) {
      randomChars[i] = alphaNumerics[random.nextInt(alphaNumerics.length)];
    }

    return new String(randomChars);
  }
}
