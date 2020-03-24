package common;

public final class Constants {

    public static enum Branch {
        DEV(-1), ALPHA(0), BETA(1), RELEASE(2);
        
        public final int branchId;

        private Branch(int branchId) {
            this.branchId = branchId;
        }
    }
    
    private Constants() {}
    
}