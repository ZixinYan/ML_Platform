package com.ml.authserver.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class WxUtils {
    public static boolean checkSignature(String signature, String timestamp, String nonce) {
        String[] tmpArr = {ConstantWxUtils.WX_OPEN_TOKEN, timestamp, nonce};
        Arrays.sort(tmpArr);
        String tmpStr = String.join("", tmpArr);
        String sha1Str = sha1(tmpStr);
        return sha1Str != null && sha1Str.equals(signature);
    }

    private static String sha1(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

}
