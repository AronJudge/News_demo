package com.xiangxue.network.utils;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public final class TecentUtil {
    public static final String secretId = "AKIDfsO3oO9pVf48554tyQj027zw55P56BpdMp0W";
    public static final String secretKey = "lGeg7pjchn1qzarl3mccjxscwxsq6788xmrirk3i";
    private static final String CONTENT_CHARSET = "UTF-8";
    private static final String HMAC_ALGORITHM = "HmacSHA1";
    private TecentUtil(){
    }

    private static String sign(String secret, String timeStr) {
        //get signStr
        String signStr = "date: " + timeStr + "\n" + "source: " + "source";
        //get sig
        try {
            Mac mac1  = Mac.getInstance(HMAC_ALGORITHM);

            byte[] hash;
            SecretKeySpec secretKey = new SecretKeySpec(secret.getBytes(CONTENT_CHARSET), mac1.getAlgorithm());
            mac1.init(secretKey);
            hash = mac1.doFinal(signStr.getBytes(CONTENT_CHARSET));
            String sig = new String(Base64.encode(hash));
            System.out.println("signValue--->" + sig);
            return sig;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAuthorization(String timeStr){
        String sig = sign(secretKey, timeStr);
        return  "hmac id=\"" + secretId + "\", algorithm=\"hmac-sha1\", headers=\"date source\", signature=\"" + sig + "\"";
    }

    public static String getTimeStr(){
        Calendar cd = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        return sdf.format(cd.getTime());
    }
}
