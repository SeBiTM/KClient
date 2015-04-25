package kclient.module.antiadmin;

import java.io.File;
import java.io.FileWriter;
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
import kclient.tools.Logger;
import kclient.tools.Parameter;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class AntiAdminModule extends ModuleBase implements Module {
    private Parameter config;
    private List<String> admins;
    
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
        String opcode = tokens[0];
        //<editor-fold defaultstate="collapsed" desc="Load Admins">
        if (opcode.equals("k") && tokens[1].contains("Hilfe")) {
            int index = packet.indexOf("##Admins sind derzeit:#");
            if (index > 0) {
                index += "##Admins sind derzeit:#".length();
                String adminRaw = packet.substring(index);
                index = adminRaw.indexOf("##(insgesamt");
                if (index > 0) {
                    adminRaw = adminRaw.substring(0, index);
                    String[] adminSplit = adminRaw.split(", ");
                    for (String adminTmp : adminSplit) {
                        //°>_h16-Red-Devil-16|/serverpp "|/w "<°
                        String adminNickname = adminTmp.substring(4);
                        index = adminNickname.indexOf("|/server");
                        if (index > 0) {
                            adminNickname = Util.escapeKCode(adminNickname.substring(0, index));
                            if (!this.admins.contains(adminNickname))
                                this.admins.add(adminNickname);
                        }
                    }
                }
                return null;
            }
        } else
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="New Protocol">
        if (opcode.equals(":")) {
            GenericProtocol node = this.groupChat.getBaseNode().read(packet, 2);
            if (node.equalsName("CHANNEL_MEMBERS")) {
                String channel = node.get("CHANNEL_NAME");
                ArrayList<String> cms = new ArrayList<>();
                if (this.admins.isEmpty())
                    this.groupChat.sendPublic(channel, "/h");
                
                ArrayList<String> cusers = new ArrayList<>();
                ArrayList<GenericProtocol> members = node.get("CHANNEL_MEMBER");
                for (GenericProtocol member : members) {
                    String nickname = member.get("NAME");
                    ArrayList<GenericProtocol> icons = member.get("NICKLIST_ICON");
                    for (GenericProtocol icon : icons) {
                        String image = icon.get("IMAGE");
                        if (image.endsWith("cm.png")) {
                            if (!cms.contains(nickname))
                                cms.add(nickname);
                        }
                    }
                    cusers.add(nickname);
                }
                this.checkWarnBuffer(channel, cms, cusers);
            } else if (node.equalsName("ADD_CHANNEL_MEMBER")) {
                String channel = node.get("CHANNEL_NAME");
                String nickname = node.get("NAME");
                boolean isCM = false;
                boolean isAdmin = this.admins.contains(nickname);
                ArrayList<GenericProtocol> icons = node.get("NICKLIST_ICON");
                if (icons != null)
                    for (GenericProtocol icon : icons) {
                        String image = icon.get("IMAGE");
                        if (image.endsWith("cm.png")) {
                            isCM = true;
                            break;
                        }
                    }
                
                if (isCM || isAdmin) {
                    
                }
            }
        } else 
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="Old Protocol">
        if (opcode.equals("u")) {
            if (this.admins.isEmpty())
                this.groupChat.sendPublic(tokens[1], "/h");
            
            String channel = tokens[1];
            String rawPacket = packet.substring(tokens[1].length() + 3);
            String[] splitRaw = rawPacket.split("\u0000-\u0000");
            ArrayList<String> cusers = new ArrayList<>();
            ArrayList<String> cms = new ArrayList<>();
            for (String userRaw : splitRaw) {
                String[] usr = userRaw.split("\u0000");
                String nickname = usr[0].split("\n")[0];
                for (int i = 3; i < usr.length; i++) {
                    if (usr[i].endsWith("cm.png")) {
                        cms.add(nickname);
                        break;
                    }
                }
                cusers.add(nickname);
            }
            this.checkWarnBuffer(channel, cms, cusers);
        } else if (opcode.equals("l")) {
            String channel = tokens[1].equals("-") ? this.groupChat.getCurrentChannel() : tokens[1];
            String nickname = tokens[2].split("\n")[0];
            boolean isCM = false;
            boolean isAdmin = this.admins.contains(nickname);
            for (int i = 5; i < tokens.length; i++)
                if (tokens[i].endsWith("cm.png")) {
                    isCM = true;
                    break;
                }
            
            
        }
        //</editor-fold>
        
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
        FileWriter writer = null;
        try {
            writer = new FileWriter("data" + File.separator + "antiadmin.properties");
            Properties cfg = new Properties();
            
            cfg.store(writer, "AntiAdmin Config");
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
        this.config = new Parameter("antiadmin");
        this.admins = new ArrayList<>();
    }

    private void checkWarnBuffer(String channel, ArrayList<String> cms, ArrayList<String> cusers) {
        StringBuilder warnBuffer = new StringBuilder("_Achtung:_  Aktuell befinden sich _");
        if (!cms.isEmpty()) {
            warnBuffer.append(cms.size()).append("_ CM").append(cms.size() > 1 ? "'s" : "").append(" (");
            for (String nickname : cms) {
                System.out.println(nickname);
                warnBuffer.append("_°>_h").append(Util.escapeKCode(nickname).replace(">", "\\>").replace("<", "\\<")).append("|/w \"|/pp \"<°_, ");
            }
            warnBuffer.delete(warnBuffer.length() - 2, warnBuffer.length());
            warnBuffer.append(") ");
            StringBuilder adminBuffer = new StringBuilder();
            int acount = 0;
            for (String nickname : cusers) {
                if (this.admins.contains(nickname)) {
                    System.err.println(nickname);
                    adminBuffer.append("_°>_h").append(Util.escapeKCode(nickname).replace(">", "\\>").replace("<", "\\<")).append("|/w \"|/pp \"<°_, ");
                    acount++;
                }
            }
            if(adminBuffer.length() > 0) {
                adminBuffer.delete(adminBuffer.length() - 2, adminBuffer.length());
                warnBuffer.append(" und _").append(acount).append("_ Admin").append(acount > 1 ? "'s" : "").append(" (").append(adminBuffer).append(")");
            }
            warnBuffer.append(" im Channel!");
            this.groupChat.printBotMessage(channel, warnBuffer.toString());
        }
    }
}
