package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

public class ForceLogoutPacket extends Packet {

    public ForceLogoutPacket() {
        super(PacketId.FORCE_LOGOUT);
    }
    
}