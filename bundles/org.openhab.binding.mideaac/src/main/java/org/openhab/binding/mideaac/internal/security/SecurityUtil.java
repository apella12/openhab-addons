/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.mideaac.internal.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Security Utils.
 *
 * @author Jacek Dobrowolski - Initial Contribution
 */
public class SecurityUtil {

    private SecretKeySpec encKey = null;
    private final CloudProvider cloudProvider;
    private static Logger logger = LoggerFactory.getLogger(SecurityUtil.class);

    public SecurityUtil(CloudProvider cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public SecretKeySpec getEncKey() throws NoSuchAlgorithmException {
        if (encKey == null) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(cloudProvider.getSignKey().getBytes(StandardCharsets.US_ASCII));
            byte[] key = md.digest();
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            encKey = skeySpec;
        }

        return encKey;
    }

    public byte[] aesDecrypt(byte[] encryptData) {
        byte[] plainText = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            SecretKeySpec key = getEncKey();

            try {
                cipher.init(Cipher.DECRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                logger.warn("AES decryption error: InvalidKeyException: {}", e.getMessage());
                return null;
            }

            try {
                plainText = cipher.doFinal(encryptData);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES decryption error: IllegalBlockSizeException: {}", e.getMessage());
                return null;
            } catch (BadPaddingException e) {
                logger.warn("AES decryption error: BadPaddingException: {}", e.getMessage());
                return null;
            }

        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES decryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return null;
        } catch (NoSuchPaddingException e) {
            logger.warn("AES decryption error: NoSuchPaddingException: {}", e.getMessage());
            return null;
        }

        return plainText;
    }
}
