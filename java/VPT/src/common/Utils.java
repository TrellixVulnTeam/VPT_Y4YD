package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class Utils {
    
    public static String hash(String str) {
        return Base64.getEncoder().encodeToString(hash(str.getBytes()));
    }
    
    public static byte[] hash(byte[] data) {
        return Utils.createMD().digest(data);
    }
    
    public static MessageDigest createMD() {
        return catchNoSuchAlgorithmException(() -> MessageDigest.getInstance(Constants.HASH_MODE));
    }
    
    public static <T> T catchNoSuchAlgorithmException(NSAEFunction<T> function) {
        try {
            return function.execute();
        } catch(NoSuchAlgorithmException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    //Code from: https://stackoverflow.com/questions/767759/occurrences-of-substring-in-a-string
    public static int countStringMatches(String str, String findStr) {
        int lastIndex = 0;
        int count = 0;

        while(lastIndex != -1){

            lastIndex = str.indexOf(findStr,lastIndex);

            if(lastIndex != -1){
                count ++;
                lastIndex += findStr.length();
            }
        }
        
        return count;
    }
    
    public static interface NSAEFunction<T> {
        
        public abstract T execute() throws NoSuchAlgorithmException;
        
    }
    
    private Utils() {}
    
}