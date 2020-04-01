package common.networking.packet.packets.result;

public class LoginResultPacket extends SingleResultPacket<Boolean> {

    public LoginResultPacket(boolean result) {
        super(result, ResultType.LOGIN);
    }
    
}
        