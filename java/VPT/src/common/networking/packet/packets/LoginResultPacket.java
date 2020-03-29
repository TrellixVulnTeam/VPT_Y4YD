package common.networking.packet.packets;

public class LoginResultPacket extends SingleResultPacket<Boolean> {

    public LoginResultPacket(boolean result) {
        super(result, ResultType.LOGIN);
    }
    
}
        