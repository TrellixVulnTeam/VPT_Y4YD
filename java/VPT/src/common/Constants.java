package common;

public final class Constants {

    public static String ENCRYPTION_MODE = "AES/CBC/PKCS5Padding";
    public static String HASH_MODE = "SHA-256";
    
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