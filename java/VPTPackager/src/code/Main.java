package code;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    public static void main(String[] args) throws IOException {
        File sourceJar = new File(args[0]);
        File clientJar = new File(sourceJar.getParent() + File.separator + "VPTClient.jar");
        File serverJar = new File(sourceJar.getParent() + File.separator + "VPTServer.jar");
        clientJar.createNewFile();
        serverJar.createNewFile();
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(sourceJar));
            ZipOutputStream cos = new ZipOutputStream(new FileOutputStream(clientJar));
            ZipOutputStream sos = new ZipOutputStream(new FileOutputStream(serverJar));) {
            ZipEntry entry;
            byte[] buf = new byte[4096];
            while((entry = zis.getNextEntry()) != null) {
                boolean isClientFile = !entry.getName().startsWith("server");
                boolean isServerFile = !entry.getName().startsWith("client");
                if(isClientFile) {
                    cos.putNextEntry(new ZipEntry(entry.getName()));
                }
                if(isServerFile) {
                    sos.putNextEntry(new ZipEntry(entry.getName()));
                }
                if(entry.getName().equals("META-INF/MANIFEST.MF")) {
                    StringBuilder sb = new StringBuilder();
                    int numBytesRead;
                    while((numBytesRead = zis.read(buf)) > 0) {
                        sb.append(new String(buf, 0, numBytesRead));
                    }
                    cos.write(((sb.toString()) + "\nMain-Class: client.ClientMain").getBytes());
                    sos.write(((sb.toString()) + "\nMain-Class: server.ServerMain").getBytes());
                } else {
                    int numBytesRead;
                    while((numBytesRead = zis.read(buf)) > 0) {
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
}
