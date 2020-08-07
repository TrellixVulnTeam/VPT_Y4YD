package common;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.Arrays;

public class SerializableImage implements Serializable {

    private static final long serialVersionUID = -8986043784517009692L;
    
    private final byte[] encodedData;
    
    public SerializableImage(BufferedImage image) {
        int width = image.getWidth(), height = image.getHeight();
        int numEncodedBytes = 8 + (3 * (width * height));
        encodedData = new byte[numEncodedBytes];
        System.arraycopy(Utils.intToBytes(width), 0, encodedData, 0, 4);
        System.arraycopy(Utils.intToBytes(height), 0, encodedData, 4, 4);
        int idx = 8;
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                System.arraycopy(to24BPP(image.getRGB(x, y)), 0, encodedData, idx, 3);
                idx += 3;
            }
        }
    }

    public byte[] getEncodedData() {
        return Arrays.copyOf(encodedData, encodedData.length);
    }
    
    public BufferedImage export() {
        int width = Utils.bytesToInt(Arrays.copyOfRange(encodedData, 0, 4)), height = Utils.bytesToInt(Arrays.copyOfRange(encodedData, 4, 8));
        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        int idx = 8;
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int argb = to32BPP(Arrays.copyOfRange(encodedData, idx, idx + 3));
                out.setRGB(x, y, argb);
                idx += 3;
            }
        }
        return out;
    }
    
    public int[] exportToSDL() {
        int width = Utils.bytesToInt(Arrays.copyOfRange(encodedData, 0, 4)), height = Utils.bytesToInt(Arrays.copyOfRange(encodedData, 4, 8));
        int[] outArr = new int[(width * height) + 2];
        outArr[0] = width;
        outArr[1] = height;
        int iidx = 8;
        int idx = 2;
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                int argb = to32BPP(Arrays.copyOfRange(encodedData, iidx, iidx + 3));
                outArr[idx] = argb;
                iidx += 3;
                idx++;
            }
        }
        return outArr;
    }
    
    public static byte[] to24BPP(int argb) {
        int a = (argb >> 29) & 7;
        int r = (argb >> 17) & 127;
        int g = (argb >> 9) & 127;
        int b = (argb >> 1) & 127;
        int argb24 = (a << 29) | (r << 22) | (g << 15) | (b << 8);
        return Arrays.copyOfRange(Utils.intToBytes(argb24), 0, 3);
    }
    
    public static int to32BPP(byte[] argb) {
        int argb24 = Utils.bytesToInt(Arrays.copyOf(argb, 4));
        int a = (argb24 >> 29) & 7;
        int r = (argb24 >> 22) & 127;
        int g = (argb24 >> 15) & 127;
        int b = (argb24 >> 8) & 127;
        int argb32 = (a << 29) | (r << 17) | (g << 9) | (b << 1);
        return argb32;
    }
    
}