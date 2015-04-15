package kclient.module.fifty;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.Start;
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
public class FiftyBot extends ModuleBase implements Module {
    private boolean autojoin;
    private double risk;
    private final Map<String, FiftyProcess> processes;
    
    public FiftyBot(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
        this.processes = new HashMap<>();
        this.risk = 0.258D;
        this.autojoin = true;
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        if (tokens[0].equals("t")) {
            String channel = tokens[2].equals("-") ? this.groupChat.getCurrentChannel() : tokens[2];
            if (this.processes.containsKey(channel))
                this.processes.get(channel).processInput(packet, tokens);
        } else if (tokens[0].equals("e")) {
            if (tokens[1].equals(this.groupChat.getButlerName())) {
                String msg = tokens[3];
                String channel = tokens[2].equals("-") ? this.groupChat.getCurrentChannel() : tokens[2];
                if (msg.contains("°20RR°Die Anmeldung für Fifty! läuft")) {
                    if (this.autojoin)
                        this.groupChat.sendPublic(channel, "/d +");
   
                    if (this.processes.containsKey(channel))
                        this.processes.remove(channel);
                    this.processes.put(channel, new FiftyProcess(channel, this));
                    Logger.get().debug(String.format("Start Fifty!Process for Channel %s", channel));
                }
                if (this.processes.containsKey(channel))
                    this.processes.get(channel).processInput(packet, tokens);
            }
        } else if (tokens[0].equals("r")) {
            if (tokens[1].equals(this.groupChat.getButlerName())) {
                String channel = tokens[3].equals("-") ? this.groupChat.getCurrentChannel() : tokens[3];
                if (this.processes.containsKey(channel)) {
                    this.processes.get(channel).processInput(packet, tokens);
                }
            }
        }
        return packet;
    }
    @Override
    public String handleOutput(String packet, String[] tokens) {
        return packet;
    }

    @Override
    public GenericProtocol handleExtendOutput(GameConnection connection, GenericProtocol module) {
        return module;
    }
    @Override
    public GenericProtocol handleExtendInput(GameConnection connection, GenericProtocol module) {
        return module;
    }

    public GroupChat getGroupChat() {
        return this.groupChat;
    }
    public double getRisk() {
        return this.risk;
    }
    public boolean getAutoJoin() {
        return this.autojoin;
    }
    
    @Override
    public String getName() {
        return "Fifty!";
    }
    @Override
    public String getAuthor() {
        return "°>_hSeBi|https://u-labs.de/members/sebi-2841/<°";
    }
    @Override
    public String getDescription() {
        return "Das Fifty! Module spielt für dich vollkommen automatisch Fifty!##"
                + "°>bullet2.png<° Meldet sich automatisch beim Spiel an#"
                + "°>bullet2.png<° Sendet die Würfel durch einen Random Intervall#"
                + "°>bullet2.png<° Stricht bei einem unentschieden#"
                + "°>bullet2.png<° Neue Runde kann über die Toolbar gestartet werden#"
                + "°>bullet2.png<° Infos wann der Bot was amcht in der Toolbar";
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
    }

    @Override
    public List<Button> getButtons(String channel) {
        if (channel.contains("Fifty!")) {
            return Arrays.asList(new Button[] {
                new Button("Autojoin", "py_" + (this.autojoin?"g":"r")+".gif", "/mdl " + getName() + " autojoin:" + (this.autojoin ? "false":"true"), false),
                new Button(getName(), "py_" + (this.state ? "g" : "r") + ".gif", "/mdl " + (this.state ? "-" : "+") + getName(), false)
            });
        }
        return null;
    }
    
    @Override
    public boolean handleCommand(String cmd, String arg, String channel) {
        String[] args = arg.split(":");
        if (args[0].equalsIgnoreCase("risk")) {
            this.risk = Double.parseDouble(args[1]);
            this.groupChat.printBotMessage(channel, String.format("Fifty! Risk gesetzt (%s)", this.risk));
        } else if (args[0].equalsIgnoreCase("autojoin")) {
            this.autojoin = Boolean.parseBoolean(args[1]);
            this.groupChat.printBotMessage(channel, String.format("Fifty! Autojoin gesetzt (%s)", this.autojoin));
        }
        return true;
    }

    @Override
    public void save() {
    }

    @Override
    public void load() {
    }
}
