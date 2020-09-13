package common.networking.packet.packets;

import common.UserDataField;
import common.networking.packet.PacketId;

public class CurrentUserDataRequestPacket extends SingleDataPacket<UserDataField> {

    private static final long serialVersionUID = -1517120211339863605L;

    public CurrentUserDataRequestPacket(UserDataField dataField) {
        super(PacketId.CURRENT_USER_REQUEST, dataField);
    }
    
}