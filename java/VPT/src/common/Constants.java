package common;

public final class Constants {

    public static String ENCRYPTION_MODE = "AES/CBC/PKCS5Padding";
    public static String HASH_MODE = "SHA-256";
    public static String SIGN_MODE = "SHA256withRSA";
    public static String SECRET_KEY_FACTORY_ALGORITHM = "PBKDF2WITHHMACSHA256";
    public static byte[] PBE_KEY_SALT = {85, 28, -13, 102, -75, -16, 22, -66};
    public static int PBE_KEY_ITERATION_COUNT = 65536;
    public static int SECRET_KEY_LENGTH = 256;
    public static int ASYMETRIC_KEY_LENGTH = 2048;
    public static String SECRET_KEY_ALGORITHM = "AES";
    public static String ASYMETRIC_KEY_ALGORITHM = "RSA";
    
    public static enum Branch {
        DEV(-1), ALPHA(0), BETA(1), RELEASE(2);
        
        public final int id;

        private Branch(int id) {
            this.id = id;
        }

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