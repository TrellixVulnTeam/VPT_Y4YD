package common;

import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Signature;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public final class Utils {
    
    private static final SecretKeyFactory SECRET_KEY_FACTORY = catchNoSuchAlgorithmException(() -> SecretKeyFactory.getInstance(Constants.SECRET_KEY_FACTORY_ALGORITHM));
    private static final KeyGenerator SECRET_KEY_GENERATOR = catchNoSuchAlgorithmException(() -> KeyGenerator.getInstance(Constants.SECRET_KEY_ALGORITHM));
    private static final KeyPairGenerator ASYMETRIC_KEY_GENERATOR = catchNoSuchAlgorithmException(() -> KeyPairGenerator.getInstance(Constants.ASYMETRIC_KEY_ALGORITHM));
    
    static {
        SECRET_KEY_GENERATOR.init(Constants.SECRET_KEY_LENGTH);
        ASYMETRIC_KEY_GENERATOR.initialize(Constants.ASYMETRIC_KEY_LENGTH);
    }
    
    public static String hash(String str) {
        return Base64.getEncoder().encodeToString(hash(str.getBytes()));
    }
    
    public static byte[] hash(byte[] data) {
        return Utils.createMD().digest(data);
    }
    
    public static MessageDigest createMD() {
        return catchNoSuchAlgorithmException(() -> MessageDigest.getInstance(Constants.HASH_MODE));
    }
    
    public static Signature createSignature() {
        return catchNoSuchAlgorithmException(() -> Signature.getInstance(Constants.SIGN_MODE));
    }
    
    public static IVCipher createCipher(int op, Key key) throws InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_MODE);
            cipher.init(op, key);
            return new IVCipher(cipher, cipher.getIV());
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    public static IVCipher createCipher(int op, Key key, byte[] iv) throws InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(Constants.ENCRYPTION_MODE);
            cipher.init(op, key, new IvParameterSpec(iv));
            return new IVCipher(cipher, cipher.getIV());
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace(System.err);
            return null;
        } catch(InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e);
        }
    }
    
    public static SecretKey createPasswordKey(byte[] password) {
        try {
            Key key = SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(bytesToChars(password), Constants.PBE_KEY_SALT,
                    Constants.PBE_KEY_ITERATION_COUNT, Constants.SECRET_KEY_LENGTH));
            return new SecretKeySpec(key.getEncoded(), Constants.SECRET_KEY_ALGORITHM);
        } catch(InvalidKeySpecException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    public static SecretKey createPseudoRandomSecretKey() {
        return SECRET_KEY_GENERATOR.generateKey();
    }
    
    public static KeyPair createPseudoRandomAsymetricKey() {
        return ASYMETRIC_KEY_GENERATOR.generateKeyPair();
    }
    
    public static char[] bytesToChars(byte[] bytes) {
        char[] out = new char[bytes.length];
        for(int i = 0; i < bytes.length; i++) {
            out[i] = (char)(bytes[i] & 0xFF);
        }
        return out;
    }
    
    public static <T> T catchNoSuchAlgorithmException(NSAEFunction<T> function) {
        try {
            return function.execute();
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, x);
        return buffer.array();
    }

    public static int bytesToInt(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip(); 
        return buffer.getInt();
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
    
    public static long fromNanos(long nanos, TimeUnit unit) {
        return unit.convert(nanos, TimeUnit.NANOSECONDS);
    }
    
    public static long toNanos(long duration, TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(duration, unit);
    }

    public static String formatTimeout(long timeout) {
        Duration duration = Duration.ofNanos(timeout);
        long days = duration.toDaysPart();
        int hours = duration.toHoursPart();
        int minutes = duration.toMinutesPart();
        int seconds = duration.toSecondsPart();
        String out = "";
        if(days != 0) {
            out += days;
            out += " Days ";
        }
        if(hours != 0) {
            out += hours;
            out += " Hours ";
        }
        if(minutes != 0) {
            out += minutes;
            out += " Minutes ";
        }
        if(seconds != 0) {
            out += seconds;
            out += " Seconds ";
        }
        return out.trim();
    }
    
    public static interface NSAEFunction<T> {
        
        public T execute() throws NoSuchAlgorithmException, NoSuchPaddingException;
        
    }
    
    public static interface ConditionalTransformFunction<T> {
        
        public T transform(T o);
        
    }
    
    public static class IVCipher {
        
        public final Cipher cipher;
        public final byte[] iv;

        public IVCipher(Cipher cipher, byte[] iv) {
            this.cipher = cipher;
            this.iv = iv;
        }
        
    }
    
    private Utils() {}
    
}