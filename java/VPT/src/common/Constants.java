package common;

/**
 * Constants shared by both the client and server side of the VPT
 */
public final class Constants {

    /**
     * The algorithm to use for the SSL implementation
     */
    public static String SSL_MODE = "TLSv1.3";
    /**
     * The algorithm to use for retrieving {@link javax.crypto.Cipher} instances
     */
    public static String ENCRYPTION_MODE = "AES/CBC/PKCS5Padding";
    /**
     * The algorithm to use for retrieving {@link java.security.MessageDigest} instances
     */
    public static String HASH_MODE = "SHA-256";
    /**
     * The algorithm to use for retrieving {@link java.security.Signature} instances
     */
    public static String SIGN_MODE = "SHA256withRSA";
    /**
     * The algorithm to use for retrieving {@link javax.crypto.SecretKeyFactory} instances
     */
    public static String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WITHHMACSHA256";
    /**
     * The salt to use when generating {@link javax.crypto.spec.PBEKeySpec} instances
     */
    public static byte[] PBE_KEY_SALT = {85, 28, -13, 102, -75, -16, 22, -66};
    /**
     * The key iteration count to use when generating {@link javax.crypto.spec.PBEKeySpec} instances
     */
    public static int PBE_KEY_ITERATION_COUNT = 65536;
    /**
     * The key length to use for symmetric keys
     */
    public static int SECRET_KEY_LENGTH = 256;
    /**
     * The key length to use for asymmetric keys
     */
    public static int ASYMETRIC_KEY_LENGTH = 2048;
    /**
     * The key algorithm to use for generating symmetric keys
     */
    public static String SECRET_KEY_ALGORITHM = "AES";
    /**
     * The key algorithm to use for generating asymmetric keys
     */
    public static String ASYMETRIC_KEY_ALGORITHM = "RSA";
    
    /**
     * An enum representing the state of development of different sections of the VPT
     */
    public static enum Branch {
        /**
         * A Branch signifying the code being in development
         */
        DEV(-1),
        /**
         * A Branch signifying the code being in alpha testing
         */
        ALPHA(0),
        /**
         * A Branch signifying the code being in beta testing
         */
        BETA(1),
        /**
         * A Branch signifying the code being ready for release
         */
        RELEASE(2);
        
        /**
         * A unique id associated with this Branch
         * @see #fromId(int) 
         */
        public final int id;

        private Branch(int id) {
            this.id = id;
        }

        /**
         * Retrieves the Branch associated with the given id or <code>null</code> if none could be found.
         * @param id the {@link #id} of the Branch to retrieve 
         * @return The Branch associated with the given id or <code>null</code> if none could be found
         */
        public static Branch fromId(int id) {
            for(Branch branch: values()) {
                if(branch.id == id) {
                    return branch;
                }
            }
            return null;
        }
    }
    
    private Constants() {}
    
}