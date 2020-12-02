package xyz.theprogramsrc.superauth.global.hashing;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum HashingMethod {
    MD2("MD2"),
    MD5("MD5"),
    SHA1("SHA-1"),
    SHA256("SHA-256"),
    SHA384("SHA-384"),
    SHA512("SHA-512"),

    ;

    private final String key;

    HashingMethod(String key){
        this.key = key;
    }

    public static HashingMethod getOrDefault(String hashingMethod, HashingMethod def) {
        try{
            return valueOf(hashingMethod);
        }catch (Exception ex){
            return def;
        }
    }

    public String getKey() {
        return key;
    }

    public MessageDigest requestMD() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance(getKey());
    }

    public String hash(String input) throws NoSuchAlgorithmException {
        MessageDigest algorithm = requestMD();
        algorithm.reset();
        algorithm.update(input.getBytes());
        byte[] digest = algorithm.digest();
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest));
    }
}
