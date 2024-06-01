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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;
import java.util.stream.Stream;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mideaac.internal.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Security coding and decoding.
 *
 * @author Jacek Dobrowolski - Initial Contribution
 */
public class Security {
    // private final static String appKey = "434a209a5ce141c3b726de067835d7f0";
    // private final static String signKey = ;

    // private final static String loginKey = "ac21b9f9cbfe4ca5a88562ef25e2b768";
    // private final static String iotkey = "meicloud";
    // private final static String hmackey = "PROD_VnoClJI9aikS8dyy";

    private SecretKeySpec encKey = null;
    private Logger logger = LoggerFactory.getLogger(Security.class);
    private IvParameterSpec iv = new IvParameterSpec(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

    CloudProvider cloudProvider;

    public Security(CloudProvider cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public byte[] aes_decrypt(byte[] encrypt_data) {
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
                plainText = cipher.doFinal(encrypt_data);
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

    public byte[] aes_encrypt(byte[] plainText) {
        byte[] encryptData = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");

            SecretKeySpec key = getEncKey();

            try {
                cipher.init(Cipher.ENCRYPT_MODE, key);
            } catch (InvalidKeyException e) {
                logger.warn("AES encryption error: InvalidKeyException: {}", e.getMessage());
            }

            try {
                encryptData = cipher.doFinal(plainText);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES encryption error: IllegalBlockSizeException: {}", e.getMessage());
                return null;
            } catch (BadPaddingException e) {
                logger.warn("AES encryption error: BadPaddingException: {}", e.getMessage());
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES encryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return null;
        } catch (NoSuchPaddingException e) {
            logger.warn("AES encryption error: NoSuchPaddingException: {}", e.getMessage());
            return null;
        }

        return encryptData;
    }

    private SecretKeySpec getEncKey() throws NoSuchAlgorithmException {
        if (encKey == null) {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(cloudProvider.getSignKey().getBytes(StandardCharsets.US_ASCII));
            byte[] key = md.digest();
            SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");

            encKey = skeySpec;
        }

        return encKey;
    }

    public byte[] encode32_data(byte[] raw) {
        byte[] combine = ByteBuffer
                .allocate(raw.length + cloudProvider.getSignKey().getBytes(StandardCharsets.US_ASCII).length).put(raw)
                .put(cloudProvider.getSignKey().getBytes(StandardCharsets.US_ASCII)).array();
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
            md.update(combine);
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    public enum MsgType {
        MSGTYPE_HANDSHAKE_REQUEST(0x0),
        MSGTYPE_HANDSHAKE_RESPONSE(0x1),
        MSGTYPE_ENCRYPTED_RESPONSE(0x3),
        MSGTYPE_ENCRYPTED_REQUEST(0x6),
        MSGTYPE_TRANSPARENT(0xf);

        private final int value;

        private MsgType(int value) {
            this.value = value;
        }

        public int getId() {
            return value;
        }

        public static MsgType fromId(int id) {
            for (MsgType type : values()) {
                if (type.getId() == id) {
                    return type;
                }
            }
            return MSGTYPE_TRANSPARENT;
        }
    }

    private int request_count = 0;
    private int response_count = 0;
    private byte[] _tcp_key;

    public byte[] encode_8370(byte[] data, MsgType msgtype) {
        ByteBuffer headerBuffer = ByteBuffer.allocate(256);
        ByteBuffer dataBuffer = ByteBuffer.allocate(256);

        headerBuffer.put(new byte[] { (byte) 0x83, (byte) 0x70 });

        int size = data.length;
        int padding = 0;

        logger.trace("Size: {}", size);
        byte[] paddingData = null;
        if (msgtype == MsgType.MSGTYPE_ENCRYPTED_RESPONSE || msgtype == MsgType.MSGTYPE_ENCRYPTED_REQUEST) {
            if ((size + 2) % 16 != 0) {
                padding = 16 - (size + 2 & 0xf);
                size += padding + 32;
                logger.trace("Padding size: {}, size: {}", padding, size);
                paddingData = get_random_bytes(padding);
            }
        }
        headerBuffer.put(Utils.toBytes((short) size));

        headerBuffer.put(new byte[] { 0x20, (byte) (padding << 4 | msgtype.value) });

        if (request_count > 0xfff) {
            logger.trace("request_count is too big to convert: {}, changing request_count to 0", request_count);
            request_count = 0;
        }

        dataBuffer.put(Utils.toBytes((short) request_count));
        request_count += 1;

        dataBuffer.put(data);
        if (paddingData != null) {
            dataBuffer.put(paddingData);
        }

        headerBuffer.flip();
        byte[] finalHeader = new byte[headerBuffer.remaining()];
        headerBuffer.get(finalHeader);

        dataBuffer.flip();
        byte[] finalData = new byte[dataBuffer.remaining()];
        dataBuffer.get(finalData);

        logger.trace("Header:      {}", Utils.bytesToHex(finalHeader));

        if (msgtype == MsgType.MSGTYPE_ENCRYPTED_RESPONSE || msgtype == MsgType.MSGTYPE_ENCRYPTED_REQUEST) {
            byte[] sign = sha256(Utils.concatenateArrays(finalHeader, finalData));
            logger.trace("Sign:        {}", Utils.bytesToHex(sign));
            logger.trace("TcpKey:      {}", Utils.bytesToHex(_tcp_key));

            finalData = Utils.concatenateArrays(aes_cbc_encrypt(finalData, _tcp_key), sign);
        }

        byte[] result = Utils.concatenateArrays(finalHeader, finalData);
        return result;
    }

    public Decryption8370Result decode_8370(byte[] data) throws IOException {
        if (data.length < 6) {
            return new Decryption8370Result(new ArrayList<byte[]>(), data);
        }
        byte[] header = Arrays.copyOfRange(data, 0, 6);
        logger.trace("Header:        {}", Utils.bytesToHex(header));
        if (header[0] != (byte) 0x83 || header[1] != (byte) 0x70) {
            logger.warn("Not an 8370 message");
            return new Decryption8370Result(new ArrayList<byte[]>(), data);
        }
        ByteBuffer dataBuffer = ByteBuffer.wrap(data);
        int size = dataBuffer.getShort(2) + 8;
        logger.trace("Size: {}", size);
        byte[] leftover = null;
        if (data.length < size) {
            return new Decryption8370Result(new ArrayList<byte[]>(), data);
        } else if (data.length > size) {
            leftover = Arrays.copyOfRange(data, size, data.length);
            data = Arrays.copyOfRange(data, 0, size);
        }
        int padding = header[5] >> 4;
        logger.trace("Padding: {}", padding);
        MsgType msgtype = MsgType.fromId(header[5] & 0xf);
        logger.trace("MsgType: {}", msgtype.toString());
        data = Arrays.copyOfRange(data, 6, data.length);

        if (msgtype == MsgType.MSGTYPE_ENCRYPTED_RESPONSE || msgtype == MsgType.MSGTYPE_ENCRYPTED_REQUEST) {
            byte[] sign = Arrays.copyOfRange(data, data.length - 32, data.length);
            data = Arrays.copyOfRange(data, 0, data.length - 32);
            data = aes_cbc_decrypt(data, _tcp_key);
            byte[] signLocal = sha256(Utils.concatenateArrays(header, data));

            logger.trace("Sign:        {}", Utils.bytesToHex(sign));
            logger.trace("SignLocal:   {}", Utils.bytesToHex(signLocal));
            logger.trace("TcpKey:      {}", Utils.bytesToHex(_tcp_key));
            logger.trace("Data:        {}", Utils.bytesToHex(data));

            if (Arrays.equals(sign, signLocal) != true) {
                logger.warn("Sign does not match");
                return new Decryption8370Result(new ArrayList<byte[]>(), data);
            }

            if (padding > 0) {
                data = Arrays.copyOfRange(data, 0, data.length - padding);
            }
        } else {
            logger.error("MsgType: {}", msgtype.toString());
            throw new IOException(msgtype.toString() + " response was received");
        }

        dataBuffer = ByteBuffer.wrap(data);
        response_count = dataBuffer.getShort(0);
        logger.trace("Response_count: {}", response_count);
        logger.trace("Request_count: {}", request_count);
        data = Arrays.copyOfRange(data, 2, data.length);
        if (leftover != null) {
            Decryption8370Result r = decode_8370(leftover);
            ArrayList<byte[]> responses = r.getResponses();
            responses.add(0, data);
            return new Decryption8370Result(responses, r.buffer);
        }

        ArrayList<byte[]> responses = new ArrayList<byte[]>();
        responses.add(data);
        return new Decryption8370Result(responses, new byte[] {});
    }

    public boolean tcp_key(byte[] response, byte key[]) {
        byte[] payload = Arrays.copyOfRange(response, 0, 32);
        byte[] sign = Arrays.copyOfRange(response, 32, 64);
        byte[] plain = aes_cbc_decrypt(payload, key);
        byte[] signLocal = sha256(plain);

        logger.trace("Payload:   {}", Utils.bytesToHex(payload));
        logger.trace("Sign:      {}", Utils.bytesToHex(sign));
        logger.trace("SignLocal: {}", Utils.bytesToHex(signLocal));
        logger.trace("Plain:     {}", Utils.bytesToHex(plain));

        if (Arrays.equals(sign, signLocal) != true) {
            logger.warn("Sign does not match");
            return false;
        }
        _tcp_key = Utils.strxor(plain, key);
        logger.trace("TcpKey:    {}", Utils.bytesToHex(_tcp_key));
        return true;
    }

    private byte[] aes_cbc_decrypt(byte[] encrypt_data, byte[] decrypt_key) {
        byte[] plainText = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
            SecretKeySpec key = new SecretKeySpec(decrypt_key, "AES");

            try {
                cipher.init(Cipher.DECRYPT_MODE, key, iv);
            } catch (InvalidKeyException e) {
                logger.warn("AES decryption error: InvalidKeyException: {}", e.getMessage());
                return null;
            } catch (InvalidAlgorithmParameterException e) {
                logger.warn("AES decryption error: InvalidAlgorithmParameterException: {}", e.getMessage());
                return null;
            }

            try {
                plainText = cipher.doFinal(encrypt_data);
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

    private byte[] aes_cbc_encrypt(byte[] plainText, byte[] encrypt_key) {
        byte[] encryptData = {};

        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            SecretKeySpec key = new SecretKeySpec(encrypt_key, "AES");

            try {
                cipher.init(Cipher.ENCRYPT_MODE, key, iv);
            } catch (InvalidKeyException e) {
                logger.warn("AES encryption error: InvalidKeyException: {}", e.getMessage());
            } catch (InvalidAlgorithmParameterException e) {
                logger.warn("AES encryption error: InvalidAlgorithmParameterException: {}", e.getMessage());
            }

            try {
                encryptData = cipher.doFinal(plainText);
            } catch (IllegalBlockSizeException e) {
                logger.warn("AES encryption error: IllegalBlockSizeException: {}", e.getMessage());
                return null;
            } catch (BadPaddingException e) {
                logger.warn("AES encryption error: BadPaddingException: {}", e.getMessage());
                return null;
            }
        } catch (NoSuchAlgorithmException e) {
            logger.warn("AES encryption error: NoSuchAlgorithmException: {}", e.getMessage());
            return null;
        } catch (NoSuchPaddingException e) {
            logger.warn("AES encryption error: NoSuchPaddingException: {}", e.getMessage());
            return null;
        }

        return encryptData;
    }

    private byte[] sha256(byte[] bytes) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(bytes);
        } catch (NoSuchAlgorithmException e) {
            logger.warn("SHA256 digest error: NoSuchAlgorithmException: {}", e.getMessage());
            return null;
        }
    }

    private byte[] get_random_bytes(int size) {
        byte[] random = new byte[size];
        new Random().nextBytes(random);
        return random;
    }

    public String sign(String url, JsonObject payload) {
        logger.trace("url: {}", url);
        String path;
        try {
            path = new URI(url).getPath();

            @SuppressWarnings("unused")
            Stream<Map.Entry<String, JsonElement>> sorted = payload.entrySet().stream()
                    .sorted((Map.Entry.comparingByKey()));

            String query = Utils.getQueryString(payload);

            String sign = path + query + cloudProvider.getAppKey();
            logger.trace("sign: {}", sign);
            return Utils.bytesToHexLowercase(sha256((sign).getBytes(StandardCharsets.US_ASCII)));
        } catch (URISyntaxException e) {
            logger.warn("Syntax error{}", e.getMessage());
        }

        return null;
    }

    public String new_sign(String data, String random) {
        String msg = cloudProvider.getIotKey();
        if (data != null) {
            msg += data;
        }
        msg += random;
        String sign;

        try {
            sign = hmac(msg, cloudProvider.getHmacKey(), "HmacSHA256");
        } catch (InvalidKeyException e) {
            logger.warn("HMAC digest error: InvalidKeyException: {}", e.getMessage());
            return null;
        } catch (NoSuchAlgorithmException e) {
            logger.warn("HMAC digest error: NoSuchAlgorithmException: {}", e.getMessage());
            return null;
        }

        return sign; // .hexdigest();
    }

    public String hmac(String data, String key, String algorithm) throws NoSuchAlgorithmException, InvalidKeyException {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), algorithm);
        Mac mac = Mac.getInstance(algorithm);
        mac.init(secretKeySpec);
        return Utils.bytesToHexLowercase(mac.doFinal(data.getBytes()));
    }

    public String encryptPassword(@Nullable String loginId, String password) {
        try {
            // Hash the password
            MessageDigest m = MessageDigest.getInstance("SHA-256");
            m.update(password.getBytes(StandardCharsets.US_ASCII));

            // Create the login hash with the loginID + password hash + appKey, then hash it all AGAIN
            String loginHash = loginId + Utils.bytesToHexLowercase(m.digest()) + cloudProvider.getLoginKey();
            m = MessageDigest.getInstance("SHA-256");
            m.update(loginHash.getBytes(StandardCharsets.US_ASCII));
            return Utils.bytesToHexLowercase(m.digest());
        } catch (NoSuchAlgorithmException e) {
            logger.warn("encryptPassword error: NoSuchAlgorithmException: {}", e.getMessage());
        }
        return null;
    }

    // Encrypts password for cloud API
    public String encrypt_iam_password(@Nullable String loginId, String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(password.getBytes(StandardCharsets.US_ASCII));

            MessageDigest md_second = MessageDigest.getInstance("MD5");
            md_second.update(Utils.bytesToHexLowercase(md.digest()).getBytes(StandardCharsets.US_ASCII));

            // if self._use_china_server:
            // return md_second.hexdigest()

            String login_hash = loginId + Utils.bytesToHexLowercase(md_second.digest()) + cloudProvider.getLoginKey();
            return Utils.bytesToHexLowercase(sha256(login_hash.getBytes(StandardCharsets.US_ASCII)));
        } catch (NoSuchAlgorithmException e) {
            logger.warn("encrypt_iam_passwordt error: NoSuchAlgorithmException: {}", e.getMessage());
        }
        return null;
    }

    public String getUdpId(byte[] data) {
        byte[] b = sha256(data);
        byte[] b1 = Arrays.copyOfRange(b, 0, 16);
        byte[] b2 = Arrays.copyOfRange(b, 16, b.length);
        byte[] b3 = new byte[16];
        int i = 0;
        while (i < b1.length) {
            b3[i] = (byte) (b1[i] ^ b2[i]);
            i++;
        }
        return Utils.bytesToHexLowercase(b3);
    }
}
