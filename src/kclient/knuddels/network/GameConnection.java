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
    
    public GameConnection(GroupChat groupChat, KClass instance) {
        this.groupChat = groupChat;
        this.instance = instance;
    }
    
    public void send(GenericProtocol node) {
        byte[] buffer = this.groupChat.getExtendBaseNode().toByteArray(node);
        this.instance.invokeMethod("send", buffer);
    }
    public void receive(GenericProtocol node) {
        byte[] buffer = this.groupChat.getExtendBaseNode().toByteArray(node);
        this.instance.invokeMethod("receive", buffer);
    }

    
}
