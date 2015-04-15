package kclient.knuddels.network;

import kclient.knuddels.GroupChat;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.reflection.KClass;

/**
 *
 * @author SeBi
 */
public class GameConnection {
    private final GroupChat groupChat;
    private final KClass instance;
    private String type;
    
    public GameConnection(GroupChat groupChat, KClass instance) {
        this.groupChat = groupChat;
        this.instance = instance;
        this.type = "UNKNOWN";
    }
    
    public void send(GenericProtocol node) {
        byte[] buffer = this.groupChat.getExtendBaseNode().toByteArray(node);
        this.instance.invokeMethod("send", buffer);
    }
    public void receive(GenericProtocol node) {
        byte[] buffer = this.groupChat.getExtendBaseNode().toByteArray(node);
        this.instance.invokeMethod("receive", buffer);
    }

    public void setType(String type) {
        this.type = type;
    }
    public String getType() {
        return this.type;
    }
    
    public boolean isMauMau() {
        return this.type.equals("MAUMAU");
    }
    public boolean isPoker() {
        return this.type.equals("POKER");
    }
    
}
