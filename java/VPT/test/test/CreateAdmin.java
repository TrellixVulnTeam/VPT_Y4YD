package test;

import common.SerializableImage;
import common.Utils;
import common.user.UserAttributeType;
import java.io.File;
import java.lang.reflect.Field;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import javax.imageio.ImageIO;
import server.serialization.BackupSerialization;
import server.serialization.EncryptionSerialization;
import server.serialization.MacSerialization;
import server.user.LoginService;
import server.user.SingleDataAttribute;
import server.user.User;
import server.user.UserAttribute;

/**
 * Code creating a default administrator user
 */
public class CreateAdmin {
    
    public static void main(String[] args) throws Exception {
        //Mark this thread as a system thread and disable user security
        Field systemThreadField = LoginService.class.getDeclaredField("isSystemThread");
        systemThreadField.setAccessible(true);
        ((ThreadLocal<Boolean>)systemThreadField.get(null)).set(true);
        
        //Create User
        User user = new User("admin", "password".getBytes(), true, true);
        
        //Load Attributes
        ArrayList<UserAttribute> attributes = new ArrayList<>();
        attributes.add(new SingleDataAttribute.StringSearch<>(UserAttributeType.USERNAME, "admin", user));
        System.out.println(new File("../../cpp/GraphicFrameWork/GraphicFrameWork/projects/VPT").getAbsolutePath());
        attributes.add(new SingleDataAttribute.NoSearch<>(UserAttributeType.USERICON, new SerializableImage(
                ImageIO.read(new File("../../cpp/GraphicFrameWork/GraphicFrameWork/projects/VPT/DefaultProfilePicture.png"))), user));
        attributes.forEach(user::addAttribute);
        ConcurrentHashMap<String, ArrayList<UserAttribute>> publicAttributes = new ConcurrentHashMap<>();
        publicAttributes.put(user.userId, user.getAttributes());
        BackupSerialization.serialize(publicAttributes, "Users/attributes.attrs");
        
        //Load User Keys
        KeyPair publicFileKeys = Utils.createPseudoRandomAsymetricKey();
        user.setKey("USER_FILE_PRIVATE_KEY", publicFileKeys.getPrivate());
        user.setKey("USER_FILE_PUBLIC_KEY", publicFileKeys.getPublic());
        user.setKey("ADMIN_SECRET_KEY", Utils.createPseudoRandomSecretKey());
        KeyPair adminKeys = Utils.createPseudoRandomAsymetricKey();
        user.setKey("ADMIN_PRIVATE_KEY", adminKeys.getPrivate());
        BackupSerialization.serialize(adminKeys.getPublic(), "Users/ADMIN_PUBLIC.key");
        ConcurrentHashMap<String, PublicKey> userPublicKeys = new ConcurrentHashMap<>();
        userPublicKeys.put(user.userId, publicFileKeys.getPublic());
        BackupSerialization.serialize(userPublicKeys, "Users/publickeys.pks");
        
        //Save User
        EncryptionSerialization.serialize(user, "Users/" + Utils.hash(user.userId) + ".usr", user.getKey("USER_FILE_SECRET_KEY"));
        MacSerialization.serialize(user.toNetPublicUser(), "Users/" + Utils.hash(user.userId) + ".usr.pub", (PrivateKey)user.getKey("USER_FILE_PRIVATE_KEY"));
        EncryptionSerialization.serialize(user.getKey("USER_FILE_SECRET_KEY"), "Users/" + Utils.hash(user.userId) + ".usr.key", adminKeys.getPublic());
    }
    
}