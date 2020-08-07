package core;

import common.Console;
import common.networking.packet.Packet;
import common.networking.packet.packets.ServerStatus;
import common.networking.packet.packets.result.SingleResultPacket;
import common.networking.ssl.SSLConfig;
import common.networking.ssl.SSLConnection;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

public class Main {

    public static final String SERVER_IP = "127.0.0.1";
    public static final int SERVER_PORT = 636;
    public static final int LISTEN_PORT = 0;
    public static final String ERROR_RESPONCE = "";
    
    public static void main(String[] args) {
        Console console = new Console(true, true, true);
        try {
            char[] sslKeystorePassword = console.readPassword("Enter SSL Keystore Password: ");
            KeyStore sslKeystore = KeyStore.getInstance(new File("JavaProxyPyKeystore.keystore"), sslKeystorePassword);
            KeyStore truststore = KeyStore.getInstance(new File("truststore.keystore"), "VPTtrst".toCharArray());
            SSLConfig.initBoth(sslKeystore, sslKeystorePassword, "sslkey", truststore);
        } catch(CertificateException | IOException | KeyManagementException | KeyStoreException | NoSuchAlgorithmException e) {
            e.printStackTrace();
            System.exit(1);
        }
        try {
            SSLServerSocket sc = SSLConfig.createServerSocket(LISTEN_PORT);
            while(true) {
                try {
                    SSLSocket clientSocket = (SSLSocket)sc.accept();
                    if(clientSocket.isConnected() && !clientSocket.isClosed()) {
                        BufferedReader clientInputStream;
                        BufferedWriter clientOutputStream;
                        try {
                            clientInputStream = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                            clientOutputStream = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
                        } catch(IOException e) {
                            e.printStackTrace();
                            return;
                        }
                        SSLConnection serverConnection;
                        try {
                            serverConnection = new SSLConnection(SSLConfig.createSocket(SERVER_IP, SERVER_PORT), true);
                        } catch(IOException e) {
                            try {
                                clientOutputStream.write("0");
                                clientOutputStream.flush();
                                clientSocket.close();
                            } catch(IOException exc) {
                                exc.printStackTrace();
                            }
                            e.printStackTrace();
                            return;
                        }
                        try {
                            serverConnection.pis.readDouble();
                            serverConnection.pis.readDouble();
                            serverConnection.pis.readInt();
                        } catch(IOException e) {}
                        try {
                            clientOutputStream.write(((SingleResultPacket<ServerStatus>)serverConnection.pis.readPacket()).result == ServerStatus.OK ? "1" : "0");
                            clientOutputStream.flush();
                        } catch(ClassCastException | IOException e) {
                            try {
                                clientOutputStream.write("0");
                                clientOutputStream.flush();
                                clientSocket.close();
                            } catch(IOException exc) {
                                exc.printStackTrace();
                            }
                            e.printStackTrace();
                            return;
                        }
                        new Thread(() -> {
                            while(clientSocket.isClosed() && !serverConnection.socket.isClosed()) {
                                try {
                                    String input = clientInputStream.readLine();
                                    if(input == null) {
                                        throw new EOFException();
                                    }
                                    processInput(input, serverConnection);
                                } catch(IOException e) {
                                    try {
                                        clientOutputStream.write(ERROR_RESPONCE);
                                        clientOutputStream.flush();
                                    } catch (IOException exc) {
                                    }
                                    if (e instanceof EOFException) {
                                        try {
                                            clientSocket.close();
                                            serverConnection.socket.close();
                                        } catch (IOException exc) {
                                        }
                                        return;
                                    }
                                    e.printStackTrace(System.err);
                                }
                            }
                        }).start();
                        new Thread(() -> {
                            while(clientSocket.isClosed() && !serverConnection.socket.isClosed()) {
                                try {
                                    processOutput(serverConnection.pis.readPacket(), clientOutputStream);
                                } catch (IOException e) {
                                    try {
                                        clientOutputStream.write(ERROR_RESPONCE);
                                        clientOutputStream.flush();
                                    } catch (IOException exc) {
                                    }
                                    if (e instanceof EOFException) {
                                        try {
                                            clientSocket.close();
                                            serverConnection.socket.close();
                                        } catch (IOException exc) {
                                        }
                                        return;
                                    }
                                    e.printStackTrace(System.err);
                                }
                            }
                        }).start();
                    }
                } catch(IOException e) {
                    e.printStackTrace();
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }
    
    public static void processInput(String input, SSLConnection serverConnection) throws IOException {
        
    }
    
    public static void processOutput(Packet packet, BufferedWriter clientOutputStream) throws IOException {
        
    }
    
}
