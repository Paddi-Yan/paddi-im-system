package com.paddi.common.utils;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import static com.paddi.common.constants.Constants.UserSignConstants.*;

/**
 * @Author: Paddi-Yan
 * @Project: im-system
 * @CreatedTime: 2023年07月07日 14:07:12
 */
public class SignatureUtils {

    public static void main(String[] args) {
        String signature = SignatureUtils.generateSignature(1, "s12nklj3n12$5dfds!@#", "1001", 1000L);
        System.out.println(signature);
        JSONObject object = SignatureUtils.decryptSignature(signature);
        System.out.println(object);
        JSONObject object1 = SignatureUtils.decryptSignature("eJyrVgrxCdZLrSjILEpVsjLUAXMTCwo8U*C84sz0vJDMXJC0mYWFuaGhsYERRCYzJTWvJDMtM7VIyUrJ0MDAUAmuAyjgbhRs6eztU5qZFZHv7Vds4elq4J7rnhdYHpVi6JFi7u2SamLs6OOS6B2VbatUCwDRhyhj");
        System.out.println(object1);
    }

    public static JSONObject decryptSignature(String userSign) {
        JSONObject sigDoc = new JSONObject(true);
        try {
            byte[] decodeUrlByte = Base64URL.base64DecodeUrlNotReplace(userSign.getBytes());
            byte[] decompressByte = decompress(decodeUrlByte);
            String decodeText = new String(decompressByte, "UTF-8");

            if (StringUtils.isNotBlank(decodeText)) {
                sigDoc = JSONObject.parseObject(decodeText);

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return sigDoc;
    }

    public static String generateSignature(Integer appId, String secretKey, String userId, Long expire) {
        return generateSignature(appId, secretKey, userId, expire, null);
    }

    public static String generateSignature(Integer appId, String secretKey, String userId, Long expire, byte[] userbuf) {

        long currentTime = System.currentTimeMillis() / 1000;

        JSONObject sigDoc = new JSONObject();
        sigDoc.put(TLS_IDENTIFIER, userId);
        sigDoc.put(TLS_APPID, appId);
        sigDoc.put(TLS_EXPIRE, expire);
        sigDoc.put(TLS_SIGN_TIME, currentTime);

        String base64UserBuf = null;
        if (null != userbuf) {
            base64UserBuf = Base64.getEncoder().encodeToString(userbuf).replaceAll("\\s*", "");
            sigDoc.put("TLS.userbuf", base64UserBuf);
        }
        String sig = encryptByHMACSHA256(appId, userId, secretKey,  currentTime, expire, base64UserBuf);
        if (sig.length() == 0) {
            return "";
        }
        sigDoc.put("TLS.sig", sig);
        Deflater compressor = new Deflater();
        compressor.setInput(sigDoc.toString().getBytes(StandardCharsets.UTF_8));
        compressor.finish();
        byte[] compressedBytes = new byte[2048];
        int compressedBytesLength = compressor.deflate(compressedBytes);
        compressor.end();
        return (new String(Base64URL.base64EncodeUrl(Arrays.copyOfRange(compressedBytes,
                0, compressedBytesLength)))).replaceAll("\\s*", "");
    }

    private static String encryptByHMACSHA256(Integer appId, String identifier, String secretKey, long currentTime, Long expire, String base64UserBuf) {
        String contentToBeSigned = "TLS.identifier:" + identifier + "\n"
                + "TLS.appId:" + appId + "\n"
                + "TLS.signTime:" + currentTime + "\n"
                + "TLS.expire:" + expire + "\n";
        if (null != base64UserBuf) {
            contentToBeSigned += "TLS.userbuf:" + base64UserBuf + "\n";
        }
        try {
            byte[] byteKey = secretKey.getBytes(StandardCharsets.UTF_8);
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(byteKey, "HmacSHA256");
            hmac.init(keySpec);
            byte[] byteSig = hmac.doFinal(contentToBeSigned.getBytes(StandardCharsets.UTF_8));
            return (Base64.getEncoder().encodeToString(byteSig)).replaceAll("\\s*", "");
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            return "";
        }
    }


    /**
     * 解压缩
     *
     * @param data 待压缩的数据
     * @return byte[] 解压缩后的数据
     */
    public static byte[] decompress(byte[] data) {
        byte[] output;

        Inflater decompresser = new Inflater();
        decompresser.reset();
        decompresser.setInput(data);

        ByteArrayOutputStream o = new ByteArrayOutputStream(data.length);
        try {
            byte[] buf = new byte[1024];
            while (!decompresser.finished()) {
                int i = decompresser.inflate(buf);
                o.write(buf, 0, i);
            }
            output = o.toByteArray();
        } catch (Exception e) {
            output = data;
            e.printStackTrace();
        } finally {
            try {
                o.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        decompresser.end();
        return output;
    }
}
