package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

/**
 * Represents a {@link Packet} with a piece of data attached
 * @param <T> the type of the attached data
 */
public class SingleDataPacket<T> extends Packet {
    
    private static final long serialVersionUID = 6527268264781500815L;
    
    /**
     * The data included with this SingleDataPacket
     */
    public final T data;

    /**
     * Creates a new SingleDataPacket
     * @param id The {@link PacketId} of this SingleDataPacket
     * @param data The data to include with this SingleDataPacket
     */
    public SingleDataPacket(PacketId id, T data) {
        this(id.id, data);
    }

    /**
     * Creates a new SingleDataPacket
     * @param id The id of this SingleDataPacket
     * @param data The data to include with this SingleDataPacket
     */
    public SingleDataPacket(int id, T data) {
        super(id);
        this.data = data;
    }
    
}