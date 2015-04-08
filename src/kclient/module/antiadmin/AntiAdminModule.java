package kclient.module.antiadmin;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.tools.toolbar.Button;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.tools.Parameter;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class AntiAdminModule extends ModuleBase implements Module {
    private Parameter settings;
    
    public AntiAdminModule(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
    }

    @Override
    public String getName() {
        return "AntiAdminModule";
    }
    @Override
    public String getAuthor() {
        return "SeBi";
    }
    @Override
    public String getDescription() {
        return "";
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
    }

    @Override
    public List<Button> getButtons(String channel) {
        return Arrays.asList(new Button[] {
            new Button("AntiAdmin", "/mdl " + (this.state ? "-" : "+") + getName(), "py_" + (this.state ? "g" : "r") + ".gif", false)
        });
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        if (!this.getState())
            return packet;
        String opcode = tokens[0];
        if (opcode.equals("e")) {
            String[] tmp = tokens[3].split(" ");
            for (int i = 0; i < tmp.length; i++)
                if (this.settings.containsKey(tmp[i].toLowerCase()))
                    tmp[i] = "°>" + this.settings.get(tmp[i].toLowerCase()) + "<°";
            tokens[3] = Util.join(" ", tmp);
            packet = Util.join(GroupChat.delimiter, tokens);
        } else if (opcode.equals("r")) {
            String[] tmp = tokens[4].split(" ");
            for (int i = 0; i < tmp.length; i++)
                if (this.settings.containsKey(tmp[i].toLowerCase()))
                    tmp[i] = "°>" + this.settings.get(tmp[i].toLowerCase()) + "<°";
            tokens[4] = Util.join(" ", tmp);
            packet = Util.join(GroupChat.delimiter, tokens);
        }
        return packet;
    }
    @Override
    public String handleOutput(String packet, String[] tokens) {
        if (!this.getState())
            return packet;
        String opcode = tokens[0];
        
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
        if (arg.equals("reload")) {
            this.load();
            return true;
        }
        return false;
    }

    @Override
    public void save() {
    }
    @Override
    public void load() {
        this.settings = new Parameter("module" + File.separator + "smileys");
    }
    
}
