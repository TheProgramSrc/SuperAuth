package xyz.theprogramsrc.superauth.global.hashing;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

public class Hashing {

    private static String generateSalt() {
        SecureRandom random = new SecureRandom();
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        StringBuilder sb = new StringBuilder(salt.length * 2);
        for (byte b : salt)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    public static String saltOf(String hash){
        return dataOf(hash)[1];
    }

    public static String hash(HashingMethod method, String input) throws NoSuchAlgorithmException {
        String salt = generateSalt();
        String hash = method.hash(salt + input);
        return "$" + method.name() + "$" + salt + "$" + hash;
    }

    public static boolean check(String storedData, String input) throws NoSuchAlgorithmException {
        String[] data = dataOf(storedData);
        HashingMethod method = HashingMethod.valueOf(data[0]);
        String salt = data[1];
        String hash = method.hash(salt + input);
        return data[2].equals(hash);
    }

    private static String[] dataOf(String s){
        return Arrays.copyOfRange(s.split("\\$"), 1, s.split("\\$").length);
    }

    public static boolean isHashed(String password) {
        StringBuilder v = new StringBuilder();
        for (int i = 0; i < HashingMethod.values().length; i++) {
            HashingMethod method = HashingMethod.values()[i];
            v.append(method.name());
            if(i != HashingMethod.values().length-1) v.append("|");
        }
        return password.matches("\\$("+v.toString()+")\\$*.{16}\\$(.*)");
    }
}
