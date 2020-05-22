package common.networking.packet;

import java.io.Serializable;

/**
 * A standard class for transferring data between the client and server side of the VPT
 */
public class Packet implements Serializable {
    
    private static final long serialVersionUID = -3568872218234224400L;
    
    /**
     * A Packet containing no data (this should be ignored)
     */
    public static final Packet NULL_PACKET = new Packet(PacketId.NULL);
    /**
     * A unique integer representing the type of data carried by this Packet
     * @see PacketId
     */
    public final int id;

    /**
     * Creates a new Packet
     * @param id the {@link PacketId} of this Packet
     */
    public Packet(PacketId id) {
        this(id.id);
    }
    
    /**
     * Creates a new Packet
     * @param id the id of this Packet
     */
    public Packet(int id) {
        this.id = id;
    }
    
}