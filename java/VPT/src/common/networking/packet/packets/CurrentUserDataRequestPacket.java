package common.networking.packet.packets;

import common.UserDataField;
import common.networking.packet.PacketId;

/**
 * Represents a request for information about the currently logged in user
 */
public class CurrentUserDataRequestPacket extends SingleDataPacket<UserDataField> {

    private static final long serialVersionUID = -1517120211339863605L;

    /**
     * Creates a new CurrentUserDataRequestPacket
     * @param dataField The {@link UserDataField} to request
     */
    public CurrentUserDataRequestPacket(UserDataField dataField) {
        super(PacketId.CURRENT_USER_REQUEST, dataField);
    }
    
}