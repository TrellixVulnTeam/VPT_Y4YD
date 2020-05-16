package test;

import common.Console;
import common.networking.packet.Packet;
import common.networking.packet.PacketInputStream;
import common.networking.packet.PacketOutputStream;
import common.networking.ssl.SSLConfig;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.KeyStore;
import java.util.Arrays;
import javax.net.ssl.SSLSocket;
import javax.swing.JOptionPane;

public class Main {
    
    public static void main(String[] args) throws Exception {
        KeyStore truststore = KeyStore.getInstance(new File("C:\\VPT\\truststore.keystore"), "VPTtrst".toCharArray());
        SSLConfig.initClient(truststore);
        SSLSocket socket = SSLConfig.createSocket("localhost", Integer.parseInt(JOptionPane.showInputDialog("Enter Server Port:")));
        PacketOutputStream pos = new PacketOutputStream(socket.getOutputStream());
        PacketInputStream pis = new PacketInputStream(socket.getInputStream());
        System.out.println(pis.readDouble());
        System.out.println(pis.readDouble());
        System.out.println(pis.readInt());
        Thread returnThread = new Thread(() -> {
            while(true) {
                try {
                    Packet packet = pis.readPacket();
                    Class<? extends Packet> packetClass = packet.getClass();
                    System.out.println("Reading Packet: " + packetClass.getName());
                    Field[] fields = packetClass.getFields();
                    for(Field field: fields) {
                        if(Modifier.isStatic(field.getModifiers())) {
                            continue;
                        }
                        System.out.println("Value of field: " + field.getName() + " is: \n\t" + printObject(field.get(packet)));
                    }
                } catch(Exception e) {
                    if(e instanceof EOFException) {
                        try {
                            socket.close();
                        } catch(IOException exc) {}
                    }
                    if(socket.isClosed()) {
                        System.out.println("Socket Closed");
                        break;
                    } else {
                        e.printStackTrace(System.err);
                    }
                }
            }
        });
        returnThread.setDaemon(true);
        returnThread.start();
        Console console = new Console(true, true, true);
        while(true) {
            try {
                if(process(socket, pos, console)) {
                    break;
                }
            } catch(Exception e) {
                System.err.println("Error Parsing Input");
                e.printStackTrace(System.err);
            }
        }
    }
    
    public static boolean process(SSLSocket socket, PacketOutputStream pos, Console console) throws Exception {
        String classToSend = console.readLine("Enter Name of Class to Send: ");
        if(classToSend.equals("dc")) {
            socket.close();
            return true;
        }
        Class packetClass = Class.forName("common.networking.packet.packets." + classToSend);
        if(!Packet.class.isAssignableFrom(packetClass)) {
            System.err.println("Class is Not a Packet");
            return false;
        }
        String params = console.readLine("Enter Params: ");
        if(params.isEmpty()) {
            pos.writePacket((Packet)packetClass.getConstructor().newInstance());
            return false;
        }
        String[] parts = params.split(";");
        String[] classesNames = parts[0].split(",");
        String[] values = parts[1].split(",");
        if(classesNames.length != values.length) {
            System.err.println("Invalid Input");
            return false;
        }
        Class[] classes = new Class[classesNames.length];
        for(int i = 0; i < classesNames.length; i++) {
            classes[i] = Class.forName(classesNames[i]);
        }
        for(int i = 0; i < classes.length; i++) {
            try {
                Class clazz = classes[i];
                Field primField = clazz.getDeclaredField("TYPE");
                if(primField != null) {
                    classes[i] = (Class)primField.get(null);
                }
            } catch(NoSuchFieldException e) {}
        }
        Object[] processedValues = new Object[values.length];
        for(int i = 0; i < values.length; i++) {
            String value = values[i];
            String clazz = classesNames[i];
            switch(clazz) {
                case "java.lang.String":
                    processedValues[i] = value;
                    break;
                case "java.lang.Integer":
                    processedValues[i] = Integer.parseInt(value);
                    break;
                case "java.lang.Double":
                    processedValues[i] = Double.parseDouble(value);
                    break;
                case "java.lang.Byte":
                    processedValues[i] = Byte.parseByte(value);
                    break;
                case "java.lang.Boolean":
                    processedValues[i] = Boolean.parseBoolean(value);
                    break;
                case "[Ljava.lang.Byte":
                case "[B":
                    String[] byteValsAS = value.split("\\.");
                    Byte[] byteVals = new Byte[byteValsAS.length];
                    for(int j = 0; j < byteValsAS.length; j++) {
                        byteVals[j] = Byte.parseByte(byteValsAS[j]);
                    }
                    byte[] primByteVals = new byte[byteVals.length];
                    for(int j = 0; j < byteVals.length; j++) {
                        primByteVals[j] = byteVals[j];
                    }
                    processedValues[i] = clazz.equals("[B") ? primByteVals : byteVals;
                    break;
                default:
                    System.err.println("No Process For Retrieving Object Of Class: " + clazz);
                    return false;
            }
        }
        Packet packet = (Packet)packetClass.getConstructor(classes).newInstance((Object[])processedValues);
        pos.writePacket(packet);
        return false;
    }
    
    public static String printObject(Object o) {
        return o.toString();
    }
    
}