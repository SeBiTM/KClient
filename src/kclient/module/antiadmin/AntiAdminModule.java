package kclient.module.antiadmin;

import java.util.Arrays;
import java.util.List;
import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.tools.toolbar.Button;
import kclient.module.Module;
import kclient.module.ModuleBase;

/**
 *
 * @author SeBi
 */
public class AntiAdminModule extends ModuleBase implements Module {

    public AntiAdminModule(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
    }
    
    @Override
    public String getName() {
        return "AntiAdmin";
    }
    @Override
    public String getAuthor() {
        return "SeBi";
    }
    @Override
    public String getDescription() {
        return "---";
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
    }

    @Override
    public List<Button> getButtons(String channel) {
        return Arrays.asList(new Button[] {
             new Button("AntiAdmin", "py_" + (this.state ? "g" : "r") + ".gif", "/mdl " + (this.state ? "-" : "+") + getName(), false)
        });
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        return packet;
    }
    @Override
    public String handleOutput(String packet, String[] tokens) {
        return packet;
    }

    @Override
    public GenericProtocol handleExtendInput(GameConnection connection, GenericProtocol protocol) {
        return protocol;
    }
    @Override
    public GenericProtocol handleExtendOutput(GameConnection connection, GenericProtocol protocol) {
        return protocol;
    }

    @Override
    public boolean handleCommand(String cmd, String arg, String channel) {
        return true;
    }

    @Override
    public void save() {
    }
    @Override
    public void load() {
    }
}
