package com.netease.hz.bdms.easyinsight.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class MD5MsgDigest {
    public MD5MsgDigest() {
    }

    public static String compute(String inStr) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5Bytes = md5.digest(inStr.getBytes(StandardCharsets.UTF_8));
        return toHexString(md5Bytes);
    }

    public static String compute(byte[] inStr) throws Exception {
        return toHexString(md5Compute(inStr));
    }

    public static byte[] md5Compute(byte[] inStr) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] md5Bytes = md5.digest(inStr);
        return md5Bytes;
    }

    public static String md5file(File file) throws Exception {
        FileInputStream input = new FileInputStream(file);

        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buf = new byte[4096];
            boolean var4 = false;

            int len;
            while ((len = input.read(buf)) > 0) {
                md5.update(buf, 0, len);
            }

            String var5 = toHexString(md5.digest());
            return var5;
        } finally {
            input.close();
        }
    }

    private static String toHexString(byte[] bytes) {
        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < bytes.length; ++i) {
            int val = bytes[i] & 255;
            if (val < 16) {
                hexValue.append("0");
            }

            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }
}
