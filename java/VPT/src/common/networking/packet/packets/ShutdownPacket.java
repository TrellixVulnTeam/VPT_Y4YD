package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

public class ShutdownPacket extends Packet {

    public ShutdownPacket() {
        super(PacketId.SHUTDOWN);
    }
    
}