package kclient.module.bingo;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.tools.toolbar.Button;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.tools.Logger;

/**
 *
 * @author SeBi
 */
public class BingoBot extends ModuleBase implements Module {
    private final Map<String, BingoProcess> processes;
    private boolean autoJoin;
    
    public BingoBot(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
        this.autoJoin = true;
        this.processes = new HashMap<>();
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        if (tokens[0].equals("k")) {
            if (packet.contains("Siegesruf"))
                return null;
        } else if (tokens[0].equals(":")) {
            try {
                GenericProtocol module = this.groupChat.getBaseNode().read(packet, 2);
                if (module.getName().contains("BINGO")) {
                    if (module.getName().equals("BINGO_INIT") ||
                        module.getName().equals("BINGO_UPDATE") ||
                        module.getName().equals("BINGO_SET_STATE")) 
                    {
                        String channelName = module.get("CHANNEL_NAME");
                        if (!this.processes.containsKey(channelName) && module.getName().equals("BINGO_INIT"))
                            this.processes.put(channelName, new BingoProcess(channelName, this, this.groupChat));
                        if (this.processes.containsKey(channelName))
                            this.processes.get(channelName).handle(module);
                    }
                }
            } catch (Exception e) {
                Logger.get().error(e);
            }
        }
        return packet;
    }
    @Override
    public String handleOutput(String packet, String[] tokens) {
        return packet;
    }
    
    public static long getSheetId(GenericProtocol p) {
        if (p.getName().equals("BINGO_INIT"))
            p = p.get("BINGO_SHEET");
	if (p.getName().equals("BINGO_UPDATE"))
            p = p.get("BINGO_SHEET_UPDATE");
		
	return p.get("BINGO_SHEET_ID");
    }

    @Override
    public GenericProtocol handleExtendOutput(GameConnection connection, GenericProtocol module) {
        return module;
    }
    @Override
    public GenericProtocol handleExtendInput(GameConnection connection, GenericProtocol module) {
        return module;
    }

    public boolean getAutoJoin() {
        return this.autoJoin;
    }
    
    @Override
    public String getName() {
        return "BingoBot";
    }
    @Override
    public String getAuthor() {
        return "SeBi";
    }
    @Override
    public String getDescription() {
        return "...";
    }
    @Override
    public String getVersion() {
        return "1.0";
    }
    @Override
    public boolean getState() {
        return this.state;
    }
    @Override
    public void setState(boolean v) {
        this.state = v;
    }
    @Override
    public List<Button> getButtons(String channel) {
        if (channel.contains("Bingo")) {
            return Arrays.asList(new Button[] {
                new Button("Bingo", "py_" + (this.state ? "g" : "r") + ".gif", "/mdl " + (this.state ? "-" : "+") + getName(), false),
                new Button("Autojoin", "py_" + (this.autoJoin?"g":"r")+".gif", "/mdl " + getName() + " autojoin:" + (this.autoJoin ? "false":"true"), false)
            });
        }
        return null;
    }

    @Override
    public boolean handleCommand(String cmd, String arg, String channel) {
        String[] args = arg.split(":");
        if (args[0].equalsIgnoreCase("autojoin")) {
            this.autoJoin = Boolean.parseBoolean(args[1]);
            this.groupChat.printBotMessage(channel, String.format("Bingo Autojoin gesetzt (%s)", this.autoJoin));
        }
        this.groupChat.refreshToolbar(channel);
        return true;
    }

    @Override
    public void save() {
    }

    @Override
    public void load() {
    }
}
