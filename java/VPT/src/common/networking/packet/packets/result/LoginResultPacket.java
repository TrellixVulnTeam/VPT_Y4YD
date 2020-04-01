package common.networking.packet.packets.result;

public class LoginResultPacket extends ResultPacket {

    public LoginResultPacket(boolean result) {
        super(ResultType.LOGIN, result, result ? null : "Invalid Login");
    }
    
}
        