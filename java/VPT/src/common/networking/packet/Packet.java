package common.networking.packet;

import java.io.Serializable;

public class Packet implements Serializable {
    
    private static final long serialVersionUID = -3568872218234224400L;
    
    public static final Packet NULL_PACKET = new Packet(-1);
    public final int id;

    public Packet(int id) {
        this.id = id;
    }
    
}