package kclient.module.bingo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.tools.toolbar.Button;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.tools.Logger;
import kclient.tools.Parameter;

/**
 *
 * @author SeBi
 */
public class BingoBot extends ModuleBase implements Module {
    private final Map<String, BingoProcess> processes;
    private boolean autoJoin;
    public int knuddels, points, rounds, sheets;
    
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
        } else if (tokens[0].equals("r") && tokens[1].equals(this.groupChat.getButlerName())) {
            String channel = tokens[3].equals("-") ? this.groupChat.getCurrentChannel() : tokens[3];
            System.out.println(channel + " - " + tokens[4]);
            if (tokens[4].contains("nimmt nicht")) {
                if (this.processes.containsKey(channel))
                    this.processes.get(channel).fixSheetError();
            }
            if (tokens[4].contains("kein Bingo erreicht")) {
                if (this.processes.containsKey(channel))
                    this.processes.get(channel).joinBingo();
            }
            if (tokens[4].contains("Runden erreicht und insgesamt ") && tokens[4].contains("Bingo-Punkte")) {
                if (this.processes.containsKey(channel)) {
                    System.out.println("JOIN BINGO !!!!");
                    this.processes.get(channel).joinBingo();
                } else {
                    System.err.println("ERRROROR");
                }
                
                if (tokens[4].contains("1 Knuddel"))
                    this.knuddels++;
                if (tokens[4].contains("25 Bingo-Punkte"))
                    this.points += 25;
                if (tokens[4].contains("10 Bingo-Punkte"))
                    this.points += 10;
                save();
            }
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
        return "Bingo";
    }
    @Override
    public String getAuthor() {
        return "°>_hSeBi|https://u-labs.de/members/sebi-2841/<°";
    }
    @Override
    public String getDescription() {
        return "Das Bingo Module spielt für dich vollkommen automatisch Bingo mit 3 Blättern##"
                + "_Anleitung:_#Gehe in einen beliebigen Bingo Channel (Bingo Solo Free), aktiviere den Autojoin (Default: true) und starte ein Spiel, den rest übernimmt der Bot";
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
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
        } else if(args[0].equalsIgnoreCase("stats")) {
            this.groupChat.printBotMessage(channel, String.format("_Stats:_#  Runden: %s#Knuddels: %s#    Punkte: %s#     Blätter: %s#Runden bis Bingo: %s", 
                    (rounds / 3), knuddels, points, sheets, (rounds / sheets)));
        }
        this.groupChat.refreshToolbar(channel);
        return true;
    }

    @Override
    public void save() {
        FileOutputStream writer = null;
        Properties props = new Properties();
        // Stats
        props.put("knuddels", String.valueOf(this.knuddels));
        props.put("points", String.valueOf(this.points));
        props.put("sheets", String.valueOf(this.sheets));
        props.put("rounds", String.valueOf(this.rounds));
        try {
            writer = new FileOutputStream("data" + File.separator + "module" + File.separator + "bingo.properties");
            props.store(writer, "KClient - Bingo Stats");
            writer.flush();
        } catch (IOException e) {
            Logger.get().error(e);
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (IOException e) {
                }
        }
    }

    @Override
    public void load() {
        if (!new File("data" + File.separator + "module" + File.separator + "bingo.properties").exists())
            save();
        Parameter stats = new Parameter("module" + File.separator + "bingo");
        this.knuddels = Integer.parseInt(stats.get("knuddels"));
        this.points = Integer.parseInt(stats.get("points"));
        this.rounds = Integer.parseInt(stats.get("rounds"));
        this.sheets = Integer.parseInt(stats.get("sheets"));
        
    }
}
