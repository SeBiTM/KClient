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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private Map<String, List<String>> cms;
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
        if (opcode.equals("k") && tokens[1].contains("Hilfe") && this.admins.isEmpty()) {
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
        } else if (opcode.equals("r")) {
            String sender = tokens[1];
            if (sender.equals(this.groupChat.getButlerName())) {
                System.err.println(tokens[4]);
                String channel = tokens[3].equals("-") ? this.groupChat.getCurrentChannel() : tokens[3];
                if (tokens[4].contains("Überprüfung auf Bot-Benutzung")) {
                    Pattern p = Pattern.compile("/ok (.*?)");
                    Matcher m = p.matcher(tokens[4]);
                    if (m.find()) {
                        this.groupChat.sendPublicDelay(channel, "/ok " + m.group(0), Util.rnd(30000, 55000));
                        //TODO CFG
                        System.err.println(m.group(0));
                    }
                }
            } else {
                String channelFrom = tokens[5].equals(" ") ? this.groupChat.getCurrentChannel() : tokens[5];
                String channel = tokens[3].equals("-") ? this.groupChat.getCurrentChannel() : tokens[3];
                if (this.admins.contains(sender)) {
                    if (config.getBoolean("admin_send_message_logout"))
                        this.groupChat.logout();
                    if (config.getBoolean("admin_send_message_message"))
                        groupChat.printBotMessage(channel, "Der _°R°Admin " + Util.escapeNick(sender) + "_§ (" + Util.escapeNick(channelFrom) + ") hat dir gerade eine private Nachricht gesendet!");
                    if (config.getBoolean("admin_send_message_sound"))
                        Util.playSound("admin_send_message");
                    if (config.getBoolean("admin_send_message_notification"))
                        Util.showNotification("Achtung Nachricht", "Der Admin " + sender + " (" + channelFrom + ") hat dir gerade eine private Nachricht gesendet!");
                }
                boolean isCM = false;
                for (Map.Entry<String, List<String>> c : this.cms.entrySet()) {
                    for (String cm : c.getValue())
                        if (cm.equals(sender)) {
                            isCM = true;
                            break;
                        }
                    if (isCM)
                        break;
                }

                if (isCM) {
                    if (config.getBoolean("cm_send_message_logout"))
                        this.groupChat.logout();
                    if (config.getBoolean("cm_send_message_message"))
                        groupChat.printBotMessage(channel, "_Achtung:_ Der _CM " + Util.escapeNick(sender) + "_§ (" + Util.escapeNick(channelFrom) + ") hat dir gerade eine private Nachricht gesendet!");
                    if (config.getBoolean("cm_send_message_sound"))
                        Util.playSound("cm_send_message");
                    if (config.getBoolean("cm_send_message_notification"))
                        Util.showNotification("Achtung Nachricht", "Der CM " + Util.escapeNick(sender) + " (" + Util.escapeNick(channelFrom) + ") hat dir gerade eine private Nachricht gesendet!");
                }
            }
        } else if (opcode.equals("u")) {
            if (this.admins.isEmpty())
                this.groupChat.sendPublic(tokens[1], "/h");
            
            String channel = tokens[1].equals("-") ? this.groupChat.getCurrentChannel() : tokens[1];
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
                        if (!this.cms.containsKey(channel))
                            this.cms.put(channel, new ArrayList<>());
                        if (!this.cms.get(channel).contains(nickname))
                            this.cms.get(channel).add(nickname);
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
                    if (!this.cms.containsKey(channel))
                        this.cms.put(channel, new ArrayList<>());
                    if (!this.cms.get(channel).contains(nickname))
                        this.cms.get(channel).add(nickname);
                    break;
                }
            
            if (isAdmin) {
                if (config.getBoolean("admin_join_message"))
                    this.groupChat.printBotMessage(channel, String.format("_Achtung:_ Der °R°Admin§ _°>_h%s|/w \"|/p \"<°_ hat gerade den Channel betreten.", Util.escapeNick(nickname)));
                if (config.getBoolean("admin_join_sound"))
                    Util.playSound("admin_joined_channel");
                if (config.getBoolean("admin_join_notification"))
                    Util.showNotification("Achtung Admin", "Der Admin " + nickname + " hat gerade den Channel _" + channel + "_ betreten.");
                if (config.getBoolean("admin_join_logout"))
                    this.groupChat.logout();
            } else if (isCM) {
                if (config.getBoolean("cm_join_message"))
                    this.groupChat.printBotMessage(channel, String.format("_Achtung:_ Der CM _°>_h%s|/w \"|/p \"<°_ hat gerade den Channel betreten.", Util.escapeNick(nickname)));
                if (config.getBoolean("cm_join_sound"))
                    Util.playSound("cm_joined_channel");
                if (config.getBoolean("cm_join_notification"))
                    Util.showNotification("Achtung Admin", "Der CM " + nickname + " hat gerade den Channel " + channel + " betreten.");
                if (config.getBoolean("cm_join_logout"))
                    this.groupChat.logout();
            }
        }
        
        if (opcode.equals(":")) {
            GenericProtocol node = this.groupChat.getBaseNode().read(packet, 2);
            if (node.equalsName("CONVERSATION_LIST")) {
                ArrayList<String> hashes = new ArrayList<>();
                
                ArrayList<GenericProtocol> conversation = node.get("CONVERSATION");
                for (GenericProtocol con : conversation) {
                    ArrayList<GenericProtocol> msgList = con.get("CONVERSATION_MESSAGE");
                    for (GenericProtocol msg : msgList) {
                        String sender = msg.getNode("SENDER").get("NICKNAME");
                        if (sender.equals(this.groupChat.getNickname()))
                            continue;
                        String text = msg.get("TEXT");
                        String hash = sender.hashCode() + "" + text.hashCode();
                        if (!hashes.contains(hash)) {
                            hashes.add(hash);
                        } else {
                            break;
                        }
                        
                        if (this.admins.contains(sender)) {
                            if (config.getBoolean("admin_send_message_logout"))
                                this.groupChat.logout();
                            if (config.getBoolean("admin_send_message_message"))
                                groupChat.printBotMessage(this.groupChat.getCurrentChannel(), "Der _°R°Admin " + Util.escapeNick(sender) + "_§ hat dir gerade eine private Nachricht gesendet!");
                            if (config.getBoolean("admin_send_message_sound"))
                                Util.playSound("admin_send_message");
                            if (config.getBoolean("admin_send_message_notification"))
                                Util.showNotification("Achtung Admin Nachricht", "Der Admin " + sender + " hat dir gerade eine private Nachricht gesendet!");
                        }
                        boolean isCM = false;
                        for (Map.Entry<String, List<String>> c : this.cms.entrySet()) {
                            for (String cm : c.getValue())
                                if (cm.equals(sender)) {
                                    isCM = true;
                                    break;
                                }
                            if (isCM)
                                break;
                        }

                        if (isCM) {
                            if (config.getBoolean("cm_send_message_logout"))
                                this.groupChat.logout();
                            if (config.getBoolean("cm_send_message_message"))
                                groupChat.printBotMessage(this.groupChat.getCurrentChannel(), "_Achtung:_ Der _CM " + Util.escapeNick(sender) + "_§ hat dir gerade eine private Nachricht gesendet!");
                            if (config.getBoolean("cm_send_message_sound"))
                                Util.playSound("cm_send_message");
                            if (config.getBoolean("cm_send_message_notification"))
                                Util.showNotification("Achtung CM Nachricht", "Der CM " + sender + " hat dir gerade eine private Nachricht gesendet!");
                        }    
                    }
                }
            } else if (node.equalsName("CHANNEL_MEMBERS")) {
                String channel = node.get("CHANNEL_NAME").equals("-") ? this.groupChat.getCurrentChannel() : node.get("CHANNEL_NAME");
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
                            if (!this.cms.containsKey(channel))
                                this.cms.put(channel, new ArrayList<>());
                            if (!this.cms.get(channel).contains(nickname))
                                this.cms.get(channel).add(nickname);
                    
                            if (!cms.contains(nickname))
                                cms.add(nickname);
                        }
                    }
                    cusers.add(nickname);
                }
                this.checkWarnBuffer(channel, cms, cusers);
            } else if (node.equalsName("ADD_CHANNEL_MEMBER")) {
                String channel = node.get("CHANNEL_NAME").equals("-") ? this.groupChat.getCurrentChannel() : node.get("CHANNEL_NAME");
                GenericProtocol member = node.get("CHANNEL_MEMBER");
                String nickname = member.get("NAME");
                boolean isCM = false;
                boolean isAdmin = this.admins.contains(nickname);
                ArrayList<GenericProtocol> icons = member.get("NICKLIST_ICON");
                if (icons != null)
                    for (GenericProtocol icon : icons) {
                        String image = icon.get("IMAGE");
                        if (image.endsWith("cm.png")) {
                            isCM = true;
                            if (!this.cms.containsKey(channel))
                                this.cms.put(channel, new ArrayList<>());
                            if (!this.cms.get(channel).contains(nickname))
                                this.cms.get(channel).add(nickname);
                    
                            break;
                        }
                    }
                
                if (isAdmin) {
                    if (config.getBoolean("admin_join_message"))
                        this.groupChat.printBotMessage(channel, String.format("_Achtung:_ Der °R°Admin§ _°>_h%s|/w \"|/p \"<°_ hat gerade den Channel betreten.", Util.escapeNick(nickname)));
                    if (config.getBoolean("admin_join_sound"))
                        Util.playSound("admin_joined_channel");
                    if (config.getBoolean("admin_join_notification"))
                        Util.showNotification("Achtung Admin", "Der Admin " + nickname + " hat gerade den Channel " + channel + " betreten.");
                    if (config.getBoolean("admin_join_logout"))
                        this.groupChat.logout();
                } else if (isCM) {
                    if (config.getBoolean("cm_join_message"))
                        this.groupChat.printBotMessage(channel, String.format("_Achtung:_ Der CM _°>_h%s|/w \"|/p \"<°_ hat gerade den Channel betreten.", Util.escapeNick(nickname)));
                    if (config.getBoolean("cm_join_sound"))
                        Util.playSound("cm_joined_channel");
                    if (config.getBoolean("cm_join_notification"))
                        Util.showNotification("Achtung CM", "Der CM " + Util.escapeNick(nickname) + " hat gerade den Channel " + Util.escapeNick(channel) + " betreten.");
                    if (config.getBoolean("cm_join_logout"))
                        this.groupChat.logout();
                }
            } else if (node.equalsName("")) {
                
            }
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
            writer = new FileWriter("data" + File.separator + "module" + File.separator + "antiadmin.properties");
            Properties cfg = new Properties();
            cfg.put("admin_join_message", config.get("admin_join_message"));
            cfg.put("admin_join_sound", config.get("admin_join_sound"));
            cfg.put("admin_join_notification", config.get("admin_join_notification"));
            cfg.put("admin_join_logout", config.get("admin_join_logout"));
            
            cfg.put("admin_send_message_message", config.get("admin_send_message_message"));
            cfg.put("admin_send_message_sound", config.get("admin_send_message_sound"));
            cfg.put("admin_send_message_notification", config.get("admin_send_message_notification"));
            cfg.put("admin_send_message_logout", config.get("admin_send_message_logout"));
            
            cfg.put("cm_join_message", config.get("cm_join_message"));
            cfg.put("cm_join_sound",  config.get("cm_join_sound"));
            cfg.put("cm_join_notification",  config.get("cm_join_notification"));
            cfg.put("cm_join_logout", config.get("cm_join_logout"));
            
            cfg.put("cm_send_message_message", config.get("cm_send_message_message"));
            cfg.put("cm_send_message_sound", config.get("cm_send_message_sound"));
            cfg.put("cm_send_message_notification", config.get("cm_send_message_notification"));
            cfg.put("cm_send_message_logout", config.get("cm_send_message_logout"));
            
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
        this.config = new Parameter("module" + File.separator + "antiadmin");
        this.admins = new ArrayList<>();
        this.cms = new HashMap<>();
    }

    private void checkWarnBuffer(String channel, ArrayList<String> cms, ArrayList<String> cusers) {
        StringBuilder warnBuffer = new StringBuilder("_Achtung:_  Aktuell befinden sich _");
        if (!cms.isEmpty()) {
            warnBuffer.append(cms.size()).append("_ CM").append(cms.size() > 1 ? "'s" : "").append(" (");
            for (String nickname : cms) {
                warnBuffer.append("_°>_h").append(Util.escapeNick(nickname).replace(">", "\\>").replace("<", "\\<")).append("|/w \"|/pp \"<°_, ");
            }
            warnBuffer.delete(warnBuffer.length() - 2, warnBuffer.length());
            warnBuffer.append(") ");
            StringBuilder adminBuffer = new StringBuilder();
            int acount = 0;
            for (String nickname : cusers) {
                if (this.admins.contains(nickname)) {
                    adminBuffer.append("_°>_h").append(Util.escapeNick(nickname).replace(">", "\\>").replace("<", "\\<")).append("|/w \"|/pp \"<°_, ");
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
