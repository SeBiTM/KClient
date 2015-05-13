package kclient.module.bingo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
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
import kclient.module.bingo.tools.BingoSheetState;
import kclient.tools.Logger;
import kclient.tools.Parameter;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class BingoBot extends ModuleBase implements Module {
    private final Map<Long, BingoSheet> bingoSheets;
    private boolean autoJoin, doJoin;
    private String channel;
    private Parameter config;
    public int knuddels, points, rounds, sheets,
            waitTimeSendFieldMin, waitTimeSendFieldMax,
            waitTimeSendBingoMin, waitTimeSendBingoMax;
    
    public BingoBot(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
        this.autoJoin = true;
        this.bingoSheets = new HashMap<>();
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        if (tokens[0].equals("k")) {
            if (packet.contains("Siegesruf"))
                return null;
        } else if (tokens[0].equals("r") && tokens[1].equals(this.groupChat.getButlerName())) {
            String channel = tokens[3].equals("-") ? this.groupChat.getCurrentChannel() : tokens[3];
            if (tokens[4].contains("Runden erreicht und insgesamt ") && tokens[4].contains("Bingo-Punkte")) {
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
                        this.channel = channelName;
                        if (module.equalsName("BINGO_INIT")) {
                            final GenericProtocol sheet = module.get("BINGO_SHEET");
                            final long sheetId = BingoBot.getSheetId(sheet);
                            
                            if (this.bingoSheets.containsKey(sheetId))
                                this.bingoSheets.remove(sheetId);
                            
                            new Thread("BingoSheet[" + sheetId + "]") {
                                @Override
                                public void run() {
                                    BingoBot.this.bingoSheets.put(sheetId, new BingoSheet(BingoBot.this.groupChat, BingoBot.this, sheetId, sheet));
                                }
                            }.start();   
                        } else if (module.equalsName("BINGO_SET_STATE")) {
                            long sheetId = BingoBot.getSheetId(module);
                            BingoSheetState state = BingoSheetState.parse((byte)module.get("BINGO_SHEET_STATE_CONST"));
                            //System.out.println("[SetState -> " + sheetId + "] State: " + state);
                            if (this.bingoSheets.containsKey(sheetId) && this.bingoSheets.get(sheetId) != null) {
                                this.bingoSheets.get(sheetId).setState(state);
                                if (state != BingoSheetState.ACTIVE) {
                                    this.bingoSheets.remove(sheetId);
                                    this.groupChat.removeFrame(0, sheetId);
                                }
                            }
                            if (this.bingoSheets.isEmpty()) {
                                if (this.autoJoin) {
                                    this.doJoin = true;
                                    new Thread() {
                                        @Override
                                        public void run() {
                                            try {
                                                Thread.sleep(8500);
                                                int sheetsCount = 3;
                                                for (int i = 0; i < sheetsCount; i++) {
                                                    if (BingoBot.this.doJoin) {
                                                        if (BingoBot.this.bingoSheets.size() >= 3)
                                                            break;
                                                        BingoBot.this.groupChat.sendPublic(channel, "/bingo buy");
                                                        Thread.sleep(3000);
                                                    }
                                                }
                                                BingoBot.this.doJoin = false;
                                            } catch (InterruptedException ex) {
                                            }
                                        }
                                    }.start();
                                }
                            }
                        } else if (module.getName().equals("BINGO_UPDATE")) {
                            ArrayList tmpBingoSheets = module.get("BINGO_SHEET_UPDATE");
                            for (Object bso : tmpBingoSheets) {
                                GenericProtocol sheet = (GenericProtocol)bso;
                                long sheetId = BingoBot.getSheetId(sheet);
                                if (!this.bingoSheets.containsKey(sheetId))
                                    continue;
                                this.bingoSheets.get(sheetId).handleUpdate(sheet);
                            }
                            //messages
                            ArrayList tmpMessages = module.get("BINGO_GAME_MESSAGE");
                            for (Object tm : tmpMessages) {
                                GenericProtocol msg = (GenericProtocol)tm;
                                int msgId = msg.get("MES_ID");
                                String msgText = msg.get("TEXT");
                                if (msgText.contains("hattest _kein_ Bingo") || msgText.contains("Fehler: Das Bingo-Blatt nimmt")) {
                                    long sheetId = -1L;
                                    for (BingoSheet sheet : this.bingoSheets.values()) {
                                        if (sheet.getBingoCalled())
                                            sheetId = sheet.getId();
                                    }
                                    if (sheetId != -1L) {
                                        this.bingoSheets.remove(sheetId);
                                        this.groupChat.removeFrame(0, sheetId);
                                    } else
                                        this.bingoSheets.clear();
                                }
                                //System.out.println("Global Message -> " + msgId + " = " + msgText);
                            }

                            //history update
                            ArrayList tmpHistory = module.get("BINGO_HISTORY_UPDATE");
                            for (Object th : tmpHistory) {
                                GenericProtocol history = (GenericProtocol)th;
                                long sheetId = BingoBot.getSheetId(history);
                                if (!this.bingoSheets.containsKey(sheetId))
                                    continue;
                                this.bingoSheets.get(sheetId).handleHistoryUpdate(history);
                            }
                        }
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
    
    public String getChannel() {
        return this.channel;
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
        
        props.put("waitTimeSendFieldMin", "100");
        props.put("waitTimeSendFieldMax", "300");
        
        props.put("waitTimeSendBingoMin", "500");
        props.put("waitTimeSendBingoMax", "1500");
        
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
        
        this.config = new Parameter("module" + File.separator + "bingo");
        this.knuddels = Integer.parseInt(this.config.get("knuddels"));
        this.points = Integer.parseInt(this.config.get("points"));
        this.rounds = Integer.parseInt(this.config.get("rounds"));
        this.sheets = Integer.parseInt(this.config.get("sheets"));
        
        this.waitTimeSendFieldMin = Integer.parseInt(this.config.get("waitTimeSendFieldMin"));
        this.waitTimeSendFieldMax = Integer.parseInt(this.config.get("waitTimeSendFieldMax"));
        
        this.waitTimeSendBingoMin = Integer.parseInt(this.config.get("waitTimeSendBingoMin"));
        this.waitTimeSendBingoMax = Integer.parseInt(this.config.get("waitTimeSendBingoMax"));
    }
}
