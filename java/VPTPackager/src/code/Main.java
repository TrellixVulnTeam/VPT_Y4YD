package code;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    public static void main(String[] args) throws IOException {
        File sourceJar = new File(args[0]);
        String dir = sourceJar.getParent() + File.separator;
        File commonJar = new File(dir + "VPTCommon.jar");
        File clientJar = new File(dir + "VPTClient.jar");
        File serverJar = new File(dir + "VPTServer.jar");
        commonJar.createNewFile();
        clientJar.createNewFile();
        serverJar.createNewFile();
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceJar));
            ZipOutputStream comos = new ZipOutputStream(new FileOutputStream(commonJar));
            ZipOutputStream cos = new ZipOutputStream(new FileOutputStream(clientJar));
            ZipOutputStream sos = new ZipOutputStream(new FileOutputStream(serverJar));) {
            ZipEntry entry;
            byte[] buf = new byte[4096];
            while((entry = zis.getNextEntry()) != null) {
                boolean isConfigFile = entry.getName().startsWith("META-INF");
                boolean isCommonFile = entry.getName().startsWith("common");
                boolean isClientFile = entry.getName().startsWith("client") || isConfigFile;
                boolean isServerFile = entry.getName().startsWith("server") || isConfigFile;
                if(isCommonFile) {
                    comos.putNextEntry(new ZipEntry(entry.getName()));
                }
                if(isClientFile) {
                    cos.putNextEntry(new ZipEntry(entry.getName()));
                }
                if(isServerFile) {
                    sos.putNextEntry(new ZipEntry(entry.getName()));
                }
                if(entry.getName().equals("META-INF/MANIFEST.MF")) {
                    Manifest manifest = new Manifest(new UnclosableInputStream(zis));
                    boolean hasClassPath = manifest.getMainAttributes().containsKey(Attributes.Name.CLASS_PATH);
                    hasClassPath = hasClassPath ? manifest.getMainAttributes().get(Attributes.Name.CLASS_PATH).equals("") : hasClassPath;
                    manifest.getMainAttributes().put(Attributes.Name.CLASS_PATH, manifest.getMainAttributes().getOrDefault(Attributes.Name.CLASS_PATH, "") + (hasClassPath ? " " : "") + "VPTCommon.jar");
                    Manifest clientManifest = new Manifest(manifest);
                    Manifest serverManifest = new Manifest(manifest);
                    clientManifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "client.ClientMain");
                    serverManifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, "server.ServerMain");
                    clientManifest.write(new UnclosableOutputStream(cos));
                    serverManifest.write(new UnclosableOutputStream(sos));
                } else {
                    int numBytesRead;
                    while((numBytesRead = zis.read(buf)) > 0) {
                        if(isCommonFile) {
                            comos.write(buf, 0, numBytesRead);
                        }
                        if(isClientFile) {
                            cos.write(buf, 0, numBytesRead);
                        }
                        if(isServerFile) {
                            sos.write(buf, 0, numBytesRead);
                        }
                    }
                }
                if(isClientFile) {
                    cos.closeEntry();
                }
                if(isServerFile) {
                    sos.closeEntry();
                }
                zis.closeEntry();
            }
        }
    }
    
    public static class UnclosableInputStream extends FilterInputStream {
        
        public UnclosableInputStream(InputStream is) {
            super(is);
        }

        @Override
        public void close() throws IOException {}
        
    }
    
    public static class UnclosableOutputStream extends FilterOutputStream {
        
        public UnclosableOutputStream(OutputStream os) {
            super(os);
        }

        @Override
        public void close() throws IOException {}
        
    }
    
}
