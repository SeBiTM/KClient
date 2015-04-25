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
    private Map<String, List<String>> cms;
    
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
        } else if (opcode.equals(":")) {
            GenericProtocol node = this.groupChat.getBaseNode().read(packet, 2);
            if (node.equalsName("CHANNEL_MEMBERS")) {
                String channel = node.get("CHANNEL_NAME");
                if (this.admins.isEmpty())
                    this.groupChat.sendPublic(channel, "/h");
                if (this.cms.containsKey(channel))
                    this.cms.remove(channel);
                this.cms.put(channel, new ArrayList<>());
                
                ArrayList<String> cusers = new ArrayList<>();
                ArrayList<GenericProtocol> members = node.get("CHANNEL_MEMBER");
                for (GenericProtocol member : members) {
                    String nickname = member.get("NAME");
                    ArrayList<GenericProtocol> icons = member.get("NICKLIST_ICON");
                    for (GenericProtocol icon : icons) {
                        String image = icon.get("IMAGE");
                        if (image.endsWith("cm.png")) {
                            if (!this.cms.get(channel).contains(nickname))
                                this.cms.get(channel).add(nickname);
                        }
                    }
                    cusers.add(nickname);
                }
                StringBuilder warnBuffer = new StringBuilder("_Achtung:_  Aktuell befinden sich _");
                if (!this.cms.get(channel).isEmpty()) {
                    warnBuffer.append(this.cms.get(channel).size()).append("_ CM").append(this.cms.get(channel).size() > 1 ? "'s" : "").append(" (");
                    for (String nickname : this.cms.get(channel)) {
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
            } else if (node.equalsName("ADD_CHANNEL_MEMBER")) {
                String channel = node.get("CHANNEL_NAME");
                String nickname = node.get("NAME");
                boolean isCM = false;
                boolean isAdmin = this.admins.contains(nickname);
                ArrayList<GenericProtocol> icons = node.get("NICKLIST_ICON");
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
        } else if (opcode.equals("u")) {
            if (this.admins.isEmpty())
                this.groupChat.sendPublic(tokens[1], "/h");
            
            
        }
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
        this.cms = new HashMap<>();
    }
}
