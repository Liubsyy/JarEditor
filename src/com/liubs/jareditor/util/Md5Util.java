package com.liubs.jareditor.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author Liubsyy
 * @date 2024/5/11
 */
public class Md5Util {
    public static String md5(byte[] inputBytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(inputBytes);
            return bytesToHex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
