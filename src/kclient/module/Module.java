package kclient.module;

import java.util.List;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.tools.toolbar.Button;

/**
 *
 * @author SeBi
 */
public interface Module {
    String getName();
    String getAuthor();
    String getDescription();
    String getVersion();

    List<Button> getButtons(String channel);
    
    String handleInput(String packet, String[] tokens);
    String handleOutput(String packet, String[] tokens);
    
    GenericProtocol handleExtendInput(GameConnection connection, GenericProtocol protocol);
    GenericProtocol handleExtendOutput(GameConnection connection, GenericProtocol protocol);
    
    boolean handleCommand(String cmd, String arg, String channel);
    
    void save();
    void load();
}
