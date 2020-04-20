package common;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    
    public static final ConditionalTransformFunction<Integer> LITERAL_FUNCTION = (o) -> o | Pattern.LITERAL;
    public static final ConditionalTransformFunction<Integer> CASE_INSENSITIVE_FUNCTION = (o) -> o | Pattern.CASE_INSENSITIVE;
    public static int countStringMatches(String str, String findStr, boolean quote, boolean matchCase) {
        int flags = 0;
        flags = conditionalTransform(flags, quote, LITERAL_FUNCTION);
        flags = conditionalTransform(flags, !matchCase, CASE_INSENSITIVE_FUNCTION);
        
        Matcher matcher = Pattern.compile(findStr, flags).matcher(str);
        int count = 0;

        while(matcher.find()){
            count++;
        }
        
        return count;
    }
    
    public static <T> T conditionalTransform(T o, boolean condition, ConditionalTransformFunction<T> function) {
        return condition ? function.transform(o) : o;
    }
    
    public static interface NSAEFunction<T> {
        
        public T execute() throws NoSuchAlgorithmException;
        
    }
    
    public static interface ConditionalTransformFunction<T> {
        
        public T transform(T o);
        
    }
    
    private Utils() {}
    
}