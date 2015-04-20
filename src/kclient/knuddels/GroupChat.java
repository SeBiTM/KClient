package kclient.knuddels;

import java.awt.Component;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Level;
import javax.swing.JFrame;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.reflection.KClass;
import kclient.knuddels.reflection.KLoader;
import kclient.knuddels.tools.ChatSystem;
import kclient.knuddels.tools.CommandParser;
import kclient.knuddels.tools.toolbar.Button;
import kclient.knuddels.tools.toolbar.Toolbar;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.module.antiadmin.AntiAdminModule;
import kclient.module.bingo.BingoBot;
import kclient.module.fifty.FiftyBot;
import kclient.module.maumau.MauMauBot;
import kclient.module.quiz.QuizBot;
import kclient.module.script.ScriptModule;
import kclient.module.smileys.SmileyModule;
import kclient.module.stayonline.StayOnlineBot;
import kclient.module.wordmix.WordMixBot;
import kclient.tools.Logger;
import kclient.tools.Parameter;

/**
 *
 * @author SeBi
 */
public class GroupChat extends KClass {
    public static final String delimiter = "\u0000";
    private GenericProtocol baseNode, baseExtendNode;
    private final Parameter params;
    private final Map<String, JFrame> gameFrames;
    private final List<Module> modules;
    private String nickname, butler;
    private boolean showBingoFrames, showToolbar, showMauMauFrames, showPokerFrames;
    private final Map<String, GenericProtocol> buttonBars;
    private final Map<String, GameConnection> connections;
    private final List<String> channels;
    
    public GroupChat(ChatSystem system) {
        super(KLoader.getLoader(system), "Start");
        this.params = new Parameter(
            "chatsystem" + File.separator + 
            system.name().toLowerCase());
    
        this.showMauMauFrames = true;
        this.showPokerFrames = true;
        
        this.gameFrames = new HashMap<>();
        this.buttonBars = new HashMap<>();
        this.showToolbar = true;
        this.channels = new ArrayList<>();
        this.connections = new HashMap<>();
        
        this.modules = Arrays.asList(new Module[] {
            new SmileyModule(this), new ScriptModule(this), new WordMixBot(this),
            new FiftyBot(this), new BingoBot(this), new QuizBot(this), 
            new MauMauBot(this), new StayOnlineBot(this), new AntiAdminModule(this)
        });
        for (Module mdl : this.modules)
            mdl.load();
        
        super.invokeMethod("setTunnel", GroupChat.this);
        super.invokeMethod("init");
        super.invokeMethod("start");
    }
    
    public String handleParameter(String key) {
        if (!this.params.containsKey(key))
            return Parameter.getDefault().get(key);
        return this.params.get(key);
    }
    
    public String handleInput(String packet) {
        try {
            //Logger.get().debug(packet.replace("\0", "\\0").replace("\n", "\\n"));
            String[] tokens = packet.split(GroupChat.delimiter);
            String opcode = tokens[0];
            if (opcode.equals("5")) {
                this.butler = tokens[1];
            } else if (opcode.equals(":")) {
                if (this.baseNode == null) {
                    KClass mdlParent = new KClass(super.invokeMethod("getModuleParent"));
                    this.baseNode = GenericProtocol.parseTree((String) mdlParent.invokeMethod("getTree"));
                }
                GenericProtocol node = this.baseNode.read(packet, 2);
                if (node == null) {
                    return packet;
                }
                if (node.equalsName("CHANGE_PROTOCOL")) {
                    this.baseNode = GenericProtocol.parseTree((String) node.get("PROTOCOL_DATA"));
                } else if (node.equalsName("SHOW_BUTTONS")) {
                    if (this.buttonBars.containsKey((String)node.get("CHANNEL_NAME")))
                        this.buttonBars.remove((String)node.get("CHANNEL_NAME"));
                    this.buttonBars.put((String)node.get("CHANNEL_NAME"), node);
                    this.refreshToolbar((String)node.get("CHANNEL_NAME"));
                    return null;
                } else if (node.equalsName("CHANNEL_MEMBERS")) {
                    this.printBotMessage(tokens[1], "Hallo " + this.nickname + ", °>fullheart.png<°-lich Willkommen im _KClient_!");
                    final String channel = node.get("CHANNEL_NAME");
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                                refreshToolbar(channel);
                            } catch (InterruptedException ex) {
                            }
                        }
                    }.start();
                }
            } else if (opcode.equals("a")) {
                this.nickname = tokens[2];
                if (this.channels.contains(tokens[1]))
                    this.channels.remove(tokens[1]);
                this.channels.add(tokens[1]);
            } else if (opcode.equals("u")) {
                this.printBotMessage(tokens[1], "Hallo " + this.nickname + ", °>fullheart.png<°-lich Willkommen im _KClient_!");
                final String channel = tokens[1];
                new Thread() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(1000);
                            refreshToolbar(channel);
                        } catch (InterruptedException ex) {
                        }
                    }
                }.start();
            }
            for (Module mdl : this.modules) {
                if (((ModuleBase)mdl).getState())
                    packet = mdl.handleInput(packet, tokens);
            }
        } catch (Exception e) {
            Logger.get().error(e);
        }
        return packet;
    }
    public String handleOutput(String packet) {
        try {
            //Logger.get().debug(packet.replace("\0", "\\0").replace("\n", "\\n"));
            String[] tokens = packet.split(GroupChat.delimiter);
            String opcode = tokens[0];
            if (opcode.equals("t")) {
                this.gameFrames.clear();
                this.channels.clear();
                this.connections.clear();
            } else if (opcode.equals("e")) {
                String channel = tokens[1];
                String message = tokens[2];
                if (message.charAt(0) == '/') {
                    if (CommandParser.parse(this, message, channel))
                        return null;
                }
            } else if (opcode.equals("1")) {
                return null;
            } else if (opcode.equals("q")) {
                if (packet.contains("infoSystem") && packet.contains("slash:"))
                    return null;
                if (this.baseNode == null)
                    return packet;
            } else if (opcode.equals("w")) {
                String channel = tokens[1].equals("-") ? this.getCurrentChannel() : tokens[1];
                if (this.channels.contains(channel))
                    this.channels.remove(channel);
            } else if (opcode.equals("d")) {
                if (this.channels != null)
                    this.channels.clear();
            }
            
            for (Module mdl : this.modules) {
                if (((ModuleBase)mdl).getState())
                    packet = mdl.handleOutput(packet, tokens);
            }
        } catch (Exception e) {
            Logger.get().error(e);
        }
        return packet;
    }
    
    public byte[] handleExtendInput(KClass connection, byte[] buffer) { 
        try {
            if (this.baseExtendNode == null) {
                KClass mdlParent = new KClass(connection.invokeMethod("getModuleParent"));
                this.baseExtendNode = GenericProtocol.parseTree((String) mdlParent.invokeMethod("getTree"));
            }
            GameConnection gCon;
            if (this.connections.containsKey((String)connection.invokeMethod("toString")))
                gCon = this.connections.get((String)connection.invokeMethod("toString"));
            else {
                gCon = new GameConnection(this, connection);
                this.connections.put((String)connection.invokeMethod("toString"), gCon);
            }
            GenericProtocol node = this.baseExtendNode.read(buffer);
            if (node.equalsName("CHANGE_PROTOCOL")) {
                this.baseExtendNode = GenericProtocol.parseTree((String) node.get("PROTOCOL_DATA"));
            } else if (node.equalsName("ROOM_INIT")) {
                String gameId = node.get("GAME_ID").toString().toLowerCase();
                gCon.setType(gameId.contains("maumau") ? "MAUMAU" : gameId.contains("poker") ? "POKER" : "UNKNOWN");
            }
            for (Module mdl : this.modules)
                if (((ModuleBase)mdl).getState())
                    node = mdl.handleExtendInput(gCon, node);
            
            //buffer = this.baseExtendNode.toByteArray(node);
        } catch (Exception e) {
            Logger.get().error(e);
        }
        return buffer;
    }
    public byte[] handleExtendOutput(KClass connection, byte[] buffer) { 
        try {
            if (this.baseExtendNode == null)
                return buffer;  
            GameConnection gCon;
            if (this.connections.containsKey((String)connection.invokeMethod("toString")))
                gCon = this.connections.get((String)connection.invokeMethod("toString"));
            else{
                gCon = new GameConnection(this, connection);
                this.connections.put((String)connection.invokeMethod("toString"), gCon);
            }
            GenericProtocol node = this.baseExtendNode.read(buffer);
            for (Module mdl : this.modules)
                if (((ModuleBase)mdl).getState())
                    node = mdl.handleExtendOutput(gCon, node);
            
            //buffer = this.baseExtendNode.toByteArray(node);
        } catch (Exception e) {
            Logger.get().error(e);
        }
        return buffer;
    }
    
    public void handleFrame(int type, KClass frameClass) {
        //0 = Bingo, 1 = Poker/MauMau
        JFrame frame = (JFrame) frameClass.getInstance();
        if (type == 0) {
            final long sheetId = frameClass.get("sheetId");
            if (this.gameFrames.containsKey("BINGO" + sheetId)) {
                this.gameFrames.get("BINGO" + sheetId).dispose();
                this.gameFrames.remove("BINGO" + sheetId);
            }
            this.gameFrames.put("BINGO" + sheetId, frame);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    try {
                        GroupChat.this.removeFrame(0, sheetId);
                    } catch (Exception ex) {
                    }
                }
            });
        } else if (type == 1) {
            new Thread("WaitForTitle") {
                @Override 
                public void run() {
                    while (true) {
                        String title = frame.getTitle();
                        if (title == null || title.isEmpty()) {
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException ex) {
                            }
                            continue;
                        }
                        
                        int index = title.indexOf(":");
                        if (index > 0) {
                            final String tmpName = title.substring(index + 2);
                            index = tmpName.indexOf(",");
                            if (index > 0) {
                                String channelName = tmpName.substring(0, index);
                                if (channelName.contains("Poker")) {
                                    if (gameFrames.containsKey("POKER" + channelName))
                                        gameFrames.remove("POKER" + channelName);
                                    gameFrames.put("POKER" + channelName, frame);
                                } else if (channelName.contains("MauMau")) {
                                    if (gameFrames.containsKey("MAUMAU" + channelName))
                                        gameFrames.remove("MAUMAU" + channelName);
                                    gameFrames.put("MAUMAU" + channelName, frame);
                                }
                                
                                frame.addWindowListener(new WindowAdapter() {
                                    @Override
                                    public void windowClosing(WindowEvent e) {
                                        try {
                                            GroupChat.this.removeGameFrame(channelName);
                                        } catch (Exception ex) {
                                        }
                                    }
                                });
                            }
                        }
                        break;
                    }
                }
            }.start();
        }
    }
    public void removeFrame(int type, long sheetId) {
        String strType = type == 0 ? "BINGO" : type == 1 ? "POKER" : "MAUMAU";
        if (this.gameFrames.containsKey(strType + sheetId)) {
            this.gameFrames.get(strType + sheetId).dispose();
            this.gameFrames.remove(strType + sheetId);
        }
    }
    public void removeGameFrame(String channelName) {
        String t = channelName.contains("Poker") ? "POKER" : "MAUMAU";
        if (this.gameFrames.containsKey(t + channelName)) {
            this.gameFrames.get(t + channelName).dispose();
            this.gameFrames.remove(t + channelName);
        }
    }
    
    //<editor-fold defaultstate="collapsed" desc="Get">
    public Component getComponent() {
        return (Component) super.getInstance();
    }

    public Collection<Module> getModule() {
        return this.modules;
    }
    public Module getModule(String name) {
        for (Module mdl : this.modules)
            if (mdl.getName().equalsIgnoreCase(name)) 
                return mdl;
        return null;
    }
    
    public GenericProtocol getBaseNode() {
        return this.baseNode;
    }
    public GenericProtocol getExtendBaseNode() {
        return this.baseExtendNode;
    }
    
    public Enumeration getGameFrames() {
        return new Vector(this.gameFrames.values()).elements();
    }
    public Enumeration getBingoFrames() {
        Vector v = new Vector();
        for (Map.Entry<String, JFrame> e : this.gameFrames.entrySet()) {
            if (e.getKey().startsWith("BINGO"))
                v.add(e.getValue());
        }
        return v.elements();
    }
    public Enumeration getMauMauFrames() {
        Vector v = new Vector();
        for (Map.Entry<String, JFrame> e : this.gameFrames.entrySet())
            if (e.getKey().startsWith("MAUMAU"))
                v.add(e.getValue());
        return v.elements();
    }
    public Enumeration getPokerFrames() {
        Vector v = new Vector();
        for (Map.Entry<String, JFrame> e : this.gameFrames.entrySet())
            if (e.getKey().startsWith("POKER"))
                v.add(e.getValue());
        return v.elements();
    }
    
    public String getCurrentChannel() {
        if (this.channels == null || this.channels.isEmpty())
            return null;
        return this.channels.get(this.channels.size() - 1);
    }
    public Enumeration getChannelFrames() {
        return (Enumeration) super.invokeMethod("getChannels");
    }

    public boolean getBingoVisible() {
        return this.showBingoFrames;
    }
    public boolean getPokerVisible() {
        return this.showPokerFrames;
    }
    public boolean getMauMauVisible() {
        return this.showMauMauFrames;
    }
    
    public String getNickname() {
        return this.nickname;
    }
    public String getButlerName() {
        return this.butler;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Set">
    public void setShowMauMauFrames(boolean v) {
        this.showMauMauFrames = v;
    }
    public void setShowPokerFrames(boolean v) {
        this.showPokerFrames = v;
    }
    public void setShowBingoFrames(boolean v) {
        this.showBingoFrames = v;
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Send / Receive / Print">
    public void send(String packet) {
        super.invokeMethod("send", packet);
    }
    public void sendPublic(String channel, String msg) {
        send("e\u0000" + channel + "\u0000" + msg);
    }
    public void sendPublicDelay(final String channel, final String msg, final int sleep) {
        new Thread("SendDealy[Channel: " + channel + ", Msg: " + msg + ", Sleep: " + sleep + "]") {
            @Override
            public void run() {
                try {
                    Thread.sleep((long) sleep);
                    GroupChat.this.sendPublic(channel, msg);
                } catch (InterruptedException ex) {
                }
            }
        }.start();
    }
    
    public void receive(String packet) {
        this.invokeMethod("receive", packet);
    }
    
    public void print(String channel, String message) {
        receive("t\u0000 \u0000" + channel + "\u0000" + message);
    }
    public void printBotMessage(String channel, String message) {
        receive("r\u0000" + getButlerName() + "\u0000" + this.nickname + "\u0000" + channel + "\u0000" + message + "\u0000 ");
    }
    //</editor-fold>
    
    //<editor-fold defaultstate="collapsed" desc="Toolbar">
    public void toggleToolbar() {
        this.showToolbar = !this.showToolbar;
        for (String channel : this.channels)
            this.refreshToolbar(channel);
    }
    public void refreshToolbar(String channel, Button... buttons) {
        Toolbar bar = new Toolbar(this);
        if (buttons != null)
            for (Button btn : buttons)
                bar.addButton(btn);
        
        if (this.buttonBars.containsKey(channel)) {
            ArrayList barbuttons = ((GenericProtocol)this.buttonBars.get(channel)).get("BUTTON");
            for (int i = 0; i < barbuttons.size(); i++) {
                GenericProtocol button = (GenericProtocol)barbuttons.get(i);
                bar.addButton(new Button(
                        (String)button.get("TEXT"),
                        (String)button.get("IMAGE"),
                        (String)button.get("CHAT_FUNCTION"),
                        button.get("BUTTON_ALING") == null ? true : ((byte)button.get("BUTTON_ALING")) == 1
                ));
            }
        }
        
        for (Module b : this.modules) {
            List<Button> botButtons = b.getButtons(channel);
            if (botButtons == null)
                continue;
            for (Button but : botButtons)
                bar.addButton(but);
        }
        
        bar.refresh(channel, this.showToolbar);
    }
    //</editor-fold>
    
    public void login(String nickname, String password, String channel) {
        super.invokeMethod("login", nickname, password, channel);
    }
    
    public void stop() {
        for (Module mdl : this.modules)
            mdl.save();
        super.invokeMethod("stop");
        super.invokeMethod("destroy");
    }
}
