package common;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
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
import java.util.Base64;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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

/**
 * Provides utility functions
 */
public final class Utils {
    
    /**
     * The {@link SecretKeyFactory} used to generate secret keys from {@link SecretKeySpec} objects
     * @see Constants#SECRET_KEY_FACTORY_ALGORITHM
     */
    private static final SecretKeyFactory SECRET_KEY_FACTORY = catchNoSuchAlgorithmException(() -> SecretKeyFactory.getInstance(Constants.SECRET_KEY_FACTORY_ALGORITHM));
    
    /**
     * The {@link KeyGenerator} used to generate pseudo random secret keys
     * @see Constants#SECRET_KEY_ALGORITHM
     */
    private static final KeyGenerator SECRET_KEY_GENERATOR = catchNoSuchAlgorithmException(() -> KeyGenerator.getInstance(Constants.SECRET_KEY_ALGORITHM));
    
    /**
     * The {@link KeyPairGenerator} used to generate pseudo random key pairs
     * @see Constants#ASYMETRIC_KEY_ALGORITHM
     */
    private static final KeyPairGenerator ASYMETRIC_KEY_GENERATOR = catchNoSuchAlgorithmException(() -> KeyPairGenerator.getInstance(Constants.ASYMETRIC_KEY_ALGORITHM));
    
    static {
        SECRET_KEY_GENERATOR.init(Constants.SECRET_KEY_LENGTH);
        ASYMETRIC_KEY_GENERATOR.initialize(Constants.ASYMETRIC_KEY_LENGTH);
    }
    
    /**
     * Hashes the given string using the algorithm specified in {@link Constants#HASH_MODE} and encodes it in base64
     * @param str the string which will be hashed
     * @return the base64 encoded, hashed string
     * @see #hash(byte[]) 
     */
    public static String hash(String str) {
        return Base64.getUrlEncoder().encodeToString(hash(str.getBytes()));
    }
    
    /**
     * Hashes the given data using the algorithm specified in {@link Constants#HASH_MODE}
     * @param data the data which will be hashed
     * @return the hashed data
     * @see #hash(java.lang.String) 
     */
    public static byte[] hash(byte[] data) {
        return Utils.createMD().digest(data);
    }
    
    /**
     * A {@link NSAEFunction} which creates a {@link MessageDigest} which hashes
     * data using the algorithm specified in {@link Constants#HASH_MODE}
     * @see #createMD() 
     */
    public static final NSAEFunction<MessageDigest> DIGEST_CREATE_FUNCTION = () -> MessageDigest.getInstance(Constants.HASH_MODE);
    /**
     * Creates a {@link MessageDigest} which hashes data using the algorithm specified in {@link Constants#HASH_MODE}
     * @return a {@link MessageDigest} which hashes data using the algorithm specified in {@link Constants#HASH_MODE}
     */
    public static MessageDigest createMD() {
        return catchNoSuchAlgorithmException(DIGEST_CREATE_FUNCTION);
    }
    
    /**
     * A {@link NSAEFunction} which creates a {@link Signature} which signs
     * data using the algorithm specified in {@link Constants#SIGN_MODE}
     * @see #createMD() 
     */
    public static final NSAEFunction<Signature> SIGNATURE_CREATE_FUNCTION = () -> Signature.getInstance(Constants.SIGN_MODE);
    /**
     * Creates a {@link Signature} which signs using the algorithm specified in {@link Constants#SIGN_MODE}
     * @return a {@link Signature} which signs using the algorithm specified in {@link Constants#SIGN_MODE}
     */
    public static Signature createSignature() {
        return catchNoSuchAlgorithmException(SIGNATURE_CREATE_FUNCTION);
    }
    
    /**
     * Creates a {@link Cipher} using the encryption mode specified in {@link Constants#ASYMETRIC_ENCRYPTION_MODE}
     * @param op the opmode to initialize the Cipher with
     * @param key the key to initialize the Cipher with
     * @return A Cipher that uses the encryption mode specified in {@link Constants#ASYMETRIC_ENCRYPTION_MODE}
     * initialized with the given opmode and key.
     * @throws InvalidKeyException if the given key is invalid, or its keysize exceeds the maximum allowable keysize
     * @see Cipher#init(int, java.security.Key) 
     */
    public static Cipher createAsymetricCipher(int op, Key key) throws InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(Constants.ASYMETRIC_ENCRYPTION_MODE);
            cipher.init(op, key);
            return cipher;
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    /**
     * Creates a {@link Cipher} using the encryption mode specified in {@link Constants#SECRET_ENCRYPTION_MODE}
     * @param op the opmode to initialize the Cipher with
     * @param key the key to initialize the Cipher with
     * @return An {@link IVCipher} that uses the encryption mode specified in {@link Constants#SECRET_ENCRYPTION_MODE}
     * initialized with the given opmode and key.
     * @throws InvalidKeyException if the given key is invalid, or its keysize exceeds the maximum allowable keysize
     * @see Cipher#init(int, java.security.Key) 
     */
    public static IVCipher createCipher(int op, Key key) throws InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(Constants.SECRET_ENCRYPTION_MODE);
            cipher.init(op, key);
            return new IVCipher(cipher, cipher.getIV());
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    /**
     * Creates a {@link Cipher} using the encryption mode specified in {@link Constants#SECRET_ENCRYPTION_MODE}
     * @param op the opmode to initialize the Cipher with
     * @param key the key to initialize the Cipher with
     * @param iv the iv to initialize the cipher with
     * @return An {@link IVCipher} that uses the encryption mode specified in {@link Constants#SECRET_ENCRYPTION_MODE}
     * initialized with the given opmode and key.
     * @throws InvalidKeyException if the given key is invalid, or its keysize exceeds the maximum allowable keysize
     * @see Cipher#init(int, java.security.Key, java.security.spec.AlgorithmParameterSpec) 
     * @see IvParameterSpec
     */
    public static IVCipher createCipher(int op, Key key, byte[] iv) throws InvalidKeyException {
        try {
            Cipher cipher = Cipher.getInstance(Constants.SECRET_ENCRYPTION_MODE);
            cipher.init(op, key, new IvParameterSpec(iv));
            return new IVCipher(cipher, iv);
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace(System.err);
            return null;
        } catch(InvalidAlgorithmParameterException e) {
            throw new InvalidKeyException(e);
        }
    }
    
    /**
     * Creates a {@link SecretKey} from the given password using the PBE parameters defined in {@link Constants}
     * @param password the password to generate the key from
     * @return a PBE key based on the given password
     * @see Constants#SECRET_KEY_FACTORY_ALGORITHM
     * @see Constants#PBE_KEY_SALT
     * @see Constants#PBE_KEY_ITERATION_COUNT
     * @see Constants#SECRET_KEY_LENGTH
     * @see PBEKeySpec
     */
    public static SecretKey createPasswordKey(byte[] password) {
        try {
            Key key = SECRET_KEY_FACTORY.generateSecret(new PBEKeySpec(bytesToChars(hash(password)), Constants.PBE_KEY_SALT,
                    Constants.PBE_KEY_ITERATION_COUNT, Constants.SECRET_KEY_LENGTH));
            return new SecretKeySpec(key.getEncoded(), Constants.SECRET_KEY_ALGORITHM);
        } catch(InvalidKeySpecException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    /**
     * Creates a pseudo random {@link SecretKey} of the algorithm type specified in {@link Constants#SECRET_KEY_ALGORITHM}
     * @return a pseudo random {@link SecretKey} of the algorithm type specified in {@link Constants#SECRET_KEY_ALGORITHM}
     */
    public static SecretKey createPseudoRandomSecretKey() {
        return SECRET_KEY_GENERATOR.generateKey();
    }
    
    /**
     * Creates a pseudo random {@link KeyPair} of the algorithm type specified in {@link Constants#ASYMETRIC_KEY_ALGORITHM}
     * @return a pseudo random {@link KeyPair} of the algorithm type specified in {@link Constants#ASYMETRIC_KEY_ALGORITHM}
     */
    public static KeyPair createPseudoRandomAsymetricKey() {
        return ASYMETRIC_KEY_GENERATOR.generateKeyPair();
    }
    
    /**
     * Converts the given byte array into a char array
     * @param bytes the byte array to convert
     * @return a char array with the data contained in the given byte array
     * @throws NullPointerException if <code>bytes</code> is <code>null</code>
     */
    public static char[] bytesToChars(byte[] bytes) {
        char[] out = new char[bytes.length];
        for(int i = 0; i < bytes.length; i++) {
            out[i] = (char)(bytes[i] & 0xFF);
        }
        return out;
    }
    
    /**
     * Preforms the given function and reports any {@link NoSuchAlgorithmException}s or
     * {@link NoSuchPaddingException}s that occur to the user.
     * @param <T> the return type of the function
     * @param function the function to preform
     * @return the output of the inputted function
     */
    public static <T> T catchNoSuchAlgorithmException(NSAEFunction<T> function) {
        try {
            return function.execute();
        } catch(NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace(System.err);
            return null;
        }
    }
    
    /**
     * Converts an integer to a byte array of length 4
     * @param x the integer to convert
     * @return a byte array of length four representing the given integer
     */
    public static byte[] intToBytes(int x) {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.putInt(0, x);
        return buffer.array();
    }

    /**
     * Converts a byte array of length 4 to an integer
     * @param bytes the byte array to convert
     * @return the integer representing the byte array
     * @throws BufferOverflowException if the given byte array contains more than four bytes
     * @throws BufferUnderflowException if the given byte array contains less than four bytes
     */
    public static int bytesToInt(byte[] bytes) throws BufferOverflowException, BufferUnderflowException {
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        buffer.put(bytes, 0, bytes.length);
        buffer.flip(); 
        return buffer.getInt();
    }
    
    /**
     * A {@link TransformFunction} which applies the {@link Pattern#LITERAL} flag to its input
     */
    public static final TransformFunction<Integer> LITERAL_FUNCTION = (o) -> o | Pattern.LITERAL;
    /**
     * A {@link TransformFunction} which applies the {@link Pattern#CASE_INSENSITIVE} flag to its input
     */
    public static final TransformFunction<Integer> CASE_INSENSITIVE_FUNCTION = (o) -> o | Pattern.CASE_INSENSITIVE;
    /**
     * Counts the number of matches in the specified String
     * @param str the string to search in
     * @param findStr a string specifying what to match
     * @param quote Should the <code>findStr</code> be matched as a literal string
     * @param matchCase Should the <code>findStr</code> only match specific case
     * @return the number of matches in the specified String
     */
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
    
    /**
     * Conditionally transforms the input based on a {@link TransformFunction}
     * @param <T> the type of variable which will be operated on
     * @param o the value to conditionally transform
     * @param condition Should the value be transformed?
     * @param function a {@link TransformFunction} specifying the transformation
     * @return the conditionally transformed input
     */
    public static <T> T conditionalTransform(T o, boolean condition, TransformFunction<T> function) {
        return condition ? function.apply(o) : o;
    }
    
    /**
     * Converts from nanoseconds to the specified {@link TimeUnit}
     * @param nanos the length of time in nanoseconds
     * @param unit the {@link TimeUnit} to convert to
     * @return the equivalent duration in the specified {@link TimeUnit}
     */
    public static long fromNanos(long nanos, TimeUnit unit) {
        return unit.convert(nanos, TimeUnit.NANOSECONDS);
    }
    
    /**
     * Converts from the specified {@link TimeUnit} to nanoseconds
     * @param duration the length of time
     * @param unit the {@link TimeUnit} to convert from
     * @return the equivalent duration in nanoseconds
     */
    public static long toNanos(long duration, TimeUnit unit) {
        return TimeUnit.NANOSECONDS.convert(duration, unit);
    }
    
    /**
     * Checks if the given object is contained in the array
     * @param <T> the type of the array
     * @param arr the array to search
     * @param obj the object to search for
     * @return if the given object is contained in the array
     */
    public static <T> boolean contains(T[] arr, T obj) {
        for(T o: arr) {
            if(o.equals(obj)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * An interface representing a function which throws a {@link NoSuchAlgorithmException} or {@link NoSuchPaddingException}
     * @param <T> the type of this function
     */
    public static interface NSAEFunction<T> {
        
        /**
         * Runs this function
         * @return The result of the executed function
         * @throws NoSuchAlgorithmException if it occurs in the underlying function
         * @throws NoSuchPaddingException if it occurs in the underlying function
         */
        public T execute() throws NoSuchAlgorithmException, NoSuchPaddingException;
        
    }
    
    /**
     * An interface representing a function which transforms its input
     * @param <T> the return type of the function
     */
    public static interface TransformFunction<T> extends Function<T, T> {}
    
    /**
     * A class binding a {@link Cipher} to its iv
     * @see Cipher#getIV() 
     */
    public static class IVCipher {
        
        /**
         * The cipher used to initialize this IVCipher
         */
        public final Cipher cipher;
        /**
         * The iv of the cipher used to initialize this IVCipher
         * @see Cipher#getIV() 
         */
        public final byte[] iv;

        /**
         * Creates a new IVCipher
         * @param cipher a {@link Cipher}
         * @param iv its iv
         * @see Cipher#getIV() 
         */
        public IVCipher(Cipher cipher, byte[] iv) {
            this.cipher = cipher;
            this.iv = iv;
        }
        
    }
    
    private Utils() {}
    
}