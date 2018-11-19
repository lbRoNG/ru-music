package com.lbrong.rumusic.common.utils;

import android.util.Base64;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;

/**
 * @author lbRoNG
 * @since 2018/7/17
 */
public final class EncryptionUtils {

    private EncryptionUtils() {
    }

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

    public static String md5(byte[] buffer) {
        char hexDigits[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(buffer);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char str[] = new char[j * 2];
            int k = 0;
            for (byte byte0 : md) {
                str[k++] = hexDigits[byte0 >>> 4 & 0xf];
                str[k++] = hexDigits[byte0 & 0xf];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
}

