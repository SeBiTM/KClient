package kclient.knuddels;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
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
import kclient.module.fifty.FiftyBot;
import kclient.module.script.ScriptModule;
import kclient.module.smileys.SmileyModule;
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
    private List<JFrame> bingoFrames;
    private final List<Module> modules;
    private String nickname, butler;
    public boolean showBingoFrames, showToolbar;
    private final Map<String, GenericProtocol> buttonBars;
    private final List<String> channels;
    
    public GroupChat(ChatSystem system) {
        super(KLoader.getLoader(system), "Start");
        this.params = new Parameter(
            "chatsystem" + File.separator + 
            system.name().toLowerCase());
    
        this.buttonBars = new HashMap<>();
        this.showToolbar = true;
        this.channels = new ArrayList<>();
        
        this.modules = Arrays.asList(new Module[] {
            new SmileyModule(this), new ScriptModule(this), new WordMixBot(this),
            new FiftyBot(this)
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
                            Thread.sleep(200);
                        } catch (InterruptedException ex) {
                        }
                        refreshToolbar(channel);
                    }
                }.start();
            }
            for (Module mdl : this.modules) {
                if (((ModuleBase)mdl).getState())
                    packet = mdl.handleInput(packet, tokens);
            }
        } catch (Exception e) {
            Logger.get().error(e.toString());
        }
        return packet;
    }
    public String handleOutput(String packet) {
        try {
            //Logger.get().debug(packet.replace("\0", "\\0").replace("\n", "\\n"));
            String[] tokens = packet.split(GroupChat.delimiter);
            String opcode = tokens[0];
            if (opcode.equals("t")) {
                this.bingoFrames = new ArrayList<>();
            } else if (opcode.equals("e")) {
                String channel = tokens[1];
                String message = tokens[2];
                if (message.charAt(0) == '/') {
                    if (CommandParser.parse(this, message, channel))
                        return null;
                }
            } else if (opcode.equals("q")) {
                if (packet.contains("infoSystem") && packet.contains("slash:"))
                    return null;
                GenericProtocol node = this.baseNode.read(packet, 2);
                if (node.equalsName("REQUEST_W2")) {
                    String nick = node.get("NICK");
                    if (nick.equalsIgnoreCase("kclient")) {
                        CommandParser.parse(this, "/w kclient", getCurrentChannel());
                        return null;
                    }
                }
            }else if (opcode.equals("w")) {
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
            Logger.get().error(e.toString());
        }
        return packet;
    }
    
    public byte[] handleExtendInput(KClass connection, byte[] buffer) { 
        try {
            if (this.baseExtendNode == null) {
                KClass mdlParent = new KClass(connection.invokeMethod("getModuleParent"));
                this.baseExtendNode = GenericProtocol.parseTree((String) mdlParent.invokeMethod("getTree"));
            }
            GenericProtocol node = this.baseExtendNode.read(buffer);
            if (node.equalsName("CHANGE_PROTOCOL")) {
                this.baseExtendNode = GenericProtocol.parseTree((String) node.get("PROTOCOL_DATA"));
            }
            for (Module mdl : this.modules)
                if (((ModuleBase)mdl).getState())
                    node = mdl.handleExtendInput(new GameConnection(this, connection), node);
            
            buffer = this.baseExtendNode.toByteArray(node);
        } catch (Exception e) {
            Logger.get().error(e.toString());
        }
        return buffer;
    }
    public byte[] handleExtendOutput(KClass connection, byte[] buffer) { 
        try {
            GenericProtocol node = this.baseExtendNode.read(buffer);
            for (Module mdl : this.modules)
                if (((ModuleBase)mdl).getState())
                    node = mdl.handleExtendOutput(new GameConnection(this, connection), node);
            
            buffer = this.baseExtendNode.toByteArray(node);
        } catch (Exception e) {
            Logger.get().error(e.toString());
        }
        return buffer;
    }
    
    public void handleFrame(int type, KClass frameClass) {
        //0 = Bingo, 1 = Poker/MauMau
        JFrame frame = (JFrame) frameClass.getInstance();
        Logger.get().debug(type + " | " + frame.getTitle() + " | " + frame.toString());
    }
    
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
    
    public Enumeration getBingoFrames() {
        return (Enumeration) new Vector(this.bingoFrames);
    }
    
    public String getCurrentChannel() {
        if (this.channels == null || this.channels.isEmpty())
            return null;
        return this.channels.get(this.channels.size() - 1);
    }
    public Enumeration getChannels() {
        return (Enumeration) super.invokeMethod("getChannels");
    }

    public String getNickname() {
        return this.nickname;
    }
    public String getButlerName() {
        return this.butler;
    }
    
    public void receive(String packet) {
        this.invokeMethod("receive", packet);
    }
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
    
    public void print(String channel, String message) {
        receive("t\u0000 \u0000" + channel + "\u0000" + message);
    }
    public void printBotMessage(String channel, String message) {
        receive("r\u0000KClient\u0000" + this.nickname + "\u0000" + channel + "\u0000" + message + "\u0000 ");
    }
    
    public void refreshToolbar(String channel, Button... buttons) {
        Toolbar bar = new Toolbar(this);
        if (buttons != null)
            for (Button btn : buttons)
                bar.addButton(btn);
        if (this.buttonBars.containsKey(channel)) {
            ArrayList barbuttons = ((GenericProtocol)this.buttonBars.get(channel)).get("BUTTON");
            for (int i = 0; i < buttons.length; i++) {
                GenericProtocol button = (GenericProtocol)barbuttons.get(i);
                bar.addButton(new Button(
                    (String)button.get("TEXT"),
                    (String)button.get("CHAT_FUNCTION"),
                    (String)button.get("IMAGE"),
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
    
    public void stop() {
        for (Module mdl : this.modules)
            mdl.save();
    }
}
