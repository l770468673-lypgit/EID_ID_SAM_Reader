package com.yws.faceid.wcl;

/**
 * Created by wangcl on 2019/3/20.
 */

public class ByteUtil {
    public static String bytesToHexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder();
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();
    }

    public static byte[] hexStringToByte(String hex) {
        int len = hex.length() / 2;
        byte[] result = new byte[len];
        char[] achar = hex.toCharArray();

        for(int i = 0; i < len; ++i) {
            int pos = i * 2;
            result[i] = (byte)(toByte(achar[pos]) << 4 | toByte(achar[pos + 1]));
        }

        return result;
    }

    private static int toByte(char c) {
        byte b = (byte)"0123456789ABCDEF".indexOf(c);
        return b;
    }

    public static int byte2Int(byte b) {

        return b & 0xFF;
    }
}
