package com.rhbgroup.dte.obc.common.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchProviderException;
import java.util.Iterator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.*;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class EncryptDecryptUtil {

  public InputStream decryptFile(InputStream is, String pgpPassword, InputStream keyIn)
      throws IOException, PGPException, NoSuchProviderException {
    is = PGPUtil.getDecoderStream(is);

    PGPObjectFactory pgpF = new PGPObjectFactory(is);
    PGPEncryptedDataList enc = null;
    Object o = pgpF.nextObject();

    // the first object might be a PGP marker packet.
    if (o instanceof PGPEncryptedDataList) {
      enc = (PGPEncryptedDataList) o;
    } else {
      enc = (PGPEncryptedDataList) pgpF.nextObject();
    }

    // find the secret key
    Iterator<Object> it = enc.getEncryptedDataObjects();
    PGPPrivateKey sKey = null;
    PGPPublicKeyEncryptedData pbe = null;
    PGPSecretKeyRingCollection pgpSec =
        new PGPSecretKeyRingCollection(PGPUtil.getDecoderStream(keyIn));

    while (sKey == null && it.hasNext()) {
      pbe = (PGPPublicKeyEncryptedData) it.next();
      log.debug("pgpSec " + pgpSec);
      log.debug("pbe.getKeyID() " + pbe.getKeyID());
      log.debug("password " + pgpPassword);

      sKey = findSecretKey(pgpSec, pbe.getKeyID(), pgpPassword.toCharArray());
    }

    if (sKey == null) {
      throw new IllegalArgumentException("secret key for message not found.");
    }

    InputStream clear = pbe.getDataStream(sKey, BouncyCastleProvider.PROVIDER_NAME);
    PGPObjectFactory pgpFact = new PGPObjectFactory(clear);
    PGPCompressedData cData = (PGPCompressedData) pgpFact.nextObject();
    pgpFact = new PGPObjectFactory(cData.getDataStream());
    PGPLiteralData ld = (PGPLiteralData) pgpFact.nextObject();
    InputStream unc = ld.getInputStream();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int ch;
    while ((ch = unc.read()) >= 0) {
      out.write(ch);
    }

    byte[] returnBytes = out.toByteArray();
    out.close();
    is.close();
    return new ByteArrayInputStream(returnBytes);
  }

  private PGPPrivateKey findSecretKey(PGPSecretKeyRingCollection pgpSec, long keyID, char[] pass)
      throws PGPException, NoSuchProviderException {
    PGPSecretKey pgpSecKey = pgpSec.getSecretKey(keyID);

    if (pgpSecKey == null) {
      return null;
    }
    return pgpSecKey.extractPrivateKey(pass, BouncyCastleProvider.PROVIDER_NAME);
  }
}
