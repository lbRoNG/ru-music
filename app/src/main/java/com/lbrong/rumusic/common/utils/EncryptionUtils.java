package com.lbrong.rumusic.common.utils;

import android.support.annotation.NonNull;
import android.util.Base64;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author lbRoNG
 * @since 2018/7/17
 */
public final class EncryptionUtils {

    private EncryptionUtils() {}

    private static String getBase64(byte[] bytes) {
        String result = "";
        if (bytes != null && bytes.length != 0) {
            try {
                result = new String(Base64.encode(bytes, Base64.NO_WRAP), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public static String md5(String info) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            md5.update(info.getBytes("UTF-8"));
            byte[] md5Array = md5.digest();
            return bytesToHex1(md5Array);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            return "";
        }
    }

    @NonNull
    private static String bytesToHex1(byte[] md5Array) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte aMd5Array : md5Array) {
            int temp = 0xff & aMd5Array;
            String hexString = Integer.toHexString(temp);
            if (hexString.length() == 1) {
                strBuilder.append("0").append(hexString);
            } else {
                strBuilder.append(hexString);
            }
        }
        return strBuilder.toString();
    }
}

