package common.networking.packet.packets;

import common.networking.packet.Packet;
import common.networking.packet.PacketId;

public class ForceLogoutPacket extends Packet {

    private static final long serialVersionUID = -3820573605484580026L;
    
    public ForceLogoutPacket() {
        super(PacketId.FORCE_LOGOUT);
    }
    
}