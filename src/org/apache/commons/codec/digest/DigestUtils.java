/*
 * Decompiled with CFR 0_129.
 */
package org.apache.commons.codec.digest;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.binary.StringUtils;

public class DigestUtils {
    private static final int STREAM_BUFFER_LENGTH = 1024;

    private static byte[] digest(MessageDigest digest, InputStream data) throws IOException {
        byte[] buffer = new byte[1024];
        int read = data.read(buffer, 0, 1024);
        while (read > -1) {
            digest.update(buffer, 0, read);
            read = data.read(buffer, 0, 1024);
        }
        return digest.digest();
    }

    private static byte[] getBytesUtf8(String data) {
        return StringUtils.getBytesUtf8(data);
    }

    static MessageDigest getDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private static MessageDigest getMd5Digest() {
        return DigestUtils.getDigest("MD5");
    }

    private static MessageDigest getSha256Digest() {
        return DigestUtils.getDigest("SHA-256");
    }

    private static MessageDigest getSha384Digest() {
        return DigestUtils.getDigest("SHA-384");
    }

    private static MessageDigest getSha512Digest() {
        return DigestUtils.getDigest("SHA-512");
    }

    private static MessageDigest getShaDigest() {
        return DigestUtils.getDigest("SHA");
    }

    public static byte[] md5(byte[] data) {
        return DigestUtils.getMd5Digest().digest(data);
    }

    public static byte[] md5(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getMd5Digest(), data);
    }

    public static byte[] md5(String data) {
        return DigestUtils.md5(DigestUtils.getBytesUtf8(data));
    }

    public static String md5Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.md5(data));
    }

    public static String md5Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.md5(data));
    }

    public static String md5Hex(String data) {
        return Hex.encodeHexString(DigestUtils.md5(data));
    }

    public static byte[] sha(byte[] data) {
        return DigestUtils.getShaDigest().digest(data);
    }

    public static byte[] sha(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getShaDigest(), data);
    }

    public static byte[] sha(String data) {
        return DigestUtils.sha(DigestUtils.getBytesUtf8(data));
    }

    public static byte[] sha256(byte[] data) {
        return DigestUtils.getSha256Digest().digest(data);
    }

    public static byte[] sha256(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getSha256Digest(), data);
    }

    public static byte[] sha256(String data) {
        return DigestUtils.sha256(DigestUtils.getBytesUtf8(data));
    }

    public static String sha256Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.sha256(data));
    }

    public static String sha256Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.sha256(data));
    }

    public static String sha256Hex(String data) {
        return Hex.encodeHexString(DigestUtils.sha256(data));
    }

    public static byte[] sha384(byte[] data) {
        return DigestUtils.getSha384Digest().digest(data);
    }

    public static byte[] sha384(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getSha384Digest(), data);
    }

    public static byte[] sha384(String data) {
        return DigestUtils.sha384(DigestUtils.getBytesUtf8(data));
    }

    public static String sha384Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.sha384(data));
    }

    public static String sha384Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.sha384(data));
    }

    public static String sha384Hex(String data) {
        return Hex.encodeHexString(DigestUtils.sha384(data));
    }

    public static byte[] sha512(byte[] data) {
        return DigestUtils.getSha512Digest().digest(data);
    }

    public static byte[] sha512(InputStream data) throws IOException {
        return DigestUtils.digest(DigestUtils.getSha512Digest(), data);
    }

    public static byte[] sha512(String data) {
        return DigestUtils.sha512(DigestUtils.getBytesUtf8(data));
    }

    public static String sha512Hex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.sha512(data));
    }

    public static String sha512Hex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.sha512(data));
    }

    public static String sha512Hex(String data) {
        return Hex.encodeHexString(DigestUtils.sha512(data));
    }

    public static String shaHex(byte[] data) {
        return Hex.encodeHexString(DigestUtils.sha(data));
    }

    public static String shaHex(InputStream data) throws IOException {
        return Hex.encodeHexString(DigestUtils.sha(data));
    }

    public static String shaHex(String data) {
        return Hex.encodeHexString(DigestUtils.sha(data));
    }
}

