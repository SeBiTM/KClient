package kclient.knuddels;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import javax.swing.JFrame;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.reflection.KClass;
import kclient.knuddels.reflection.KLoader;
import kclient.knuddels.tools.ChatSystem;
import kclient.knuddels.tools.CommandParser;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.module.script.ScriptModule;
import kclient.module.smileys.SmileyModule;
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
    private String nickname;
    public boolean showBingoFrames;
    
    public GroupChat(ChatSystem system) {
        super(KLoader.getLoader(system), "Start");
        this.params = new Parameter(
            "chatsystem" + File.separator + 
            system.name().toLowerCase());
    
        this.modules = Arrays.asList(new Module[] {
            new SmileyModule(this), new ScriptModule(this)
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
    
    public void test(String str) {
        Logger.get().debug(str);
    }
    
    public String handleInput(String packet) {
        try {
            Logger.get().debug(packet.replace("\0", "\\0").replace("\n", "\\n"));
            String[] tokens = packet.split(GroupChat.delimiter);
            String opcode = tokens[0];
            if (opcode.equals(":")) {
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
                }
                //System.err.println(node);
            } else if (opcode.equals("a")) {
                this.nickname = tokens[2];
            } else if (opcode.equals("u")) {
                this.printBotMessage(tokens[1], "Hallo " + this.nickname + ", °>fullheart.png<°-lich Willkommen im _KClient_!");
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
            Logger.get().debug(packet.replace("\0", "\\0").replace("\n", "\\n"));
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
    public Enumeration getChannels() {
        return (Enumeration) super.invokeMethod("getChannels");
    }

    public void receive(String packet) {
        this.invokeMethod("receive", packet);
    }
    public void send(String packet) {
        super.invokeMethod("send", packet);
    }
    
    public void print(String channel, String message) {
        receive("t\u0000 \u0000" + channel + "\u0000" + message);
    }
    public void printBotMessage(String channel, String message) {
        receive("r\u0000KClient\u0000" + this.nickname + "\u0000" + channel + "\u0000" + message + "\u0000 ");
    }
    
    public void stop() {
        for (Module mdl : this.modules)
            mdl.save();
    }
}
