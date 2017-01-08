package bit.facetracker.tools;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by blade on 2/19/16.
 */
public class SecurityUtils {

    //SHA1
    public static String encryptToSHA(String info) {
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance("SHA-1");
            alga.update(info.getBytes());
            digesta = alga.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String rs = byte2hex(digesta);
        return rs;
    }

    private static String byte2hex(byte[] b) {
        String hs = "";// high position
        String stmp = ""; // low position
        for (int n = 0; n < b.length; n++) {
            stmp = (Integer.toHexString(b[n] & 0XFF));
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs;
    }

    public static String md5Encode(String sourceString) {

        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(sourceString.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            hash = String.valueOf(sourceString.hashCode()).getBytes();
        } catch (UnsupportedEncodingException e) {
            hash = String.valueOf(sourceString.hashCode()).getBytes();
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            int i = (b & 0xFF);
            if (i < 0x10)
                hex.append('0');
            hex.append(Integer.toHexString(i));
        }
        return hex.toString();
    }

    public static String encryptToSha1ByBase64(String input) {
        byte[] digesta = null;
        try {
            MessageDigest alga = MessageDigest.getInstance("SHA-1");
            alga.update(input.getBytes());
            digesta = alga.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        String rs = null;
        try {
            rs = Base64.encodeToString(digesta, Base64.NO_WRAP);
        } catch (Exception e) {
            System.out.print(e.toString());
        }

        return rs;
    }

}
