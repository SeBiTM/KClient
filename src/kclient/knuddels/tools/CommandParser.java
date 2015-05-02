package kclient.knuddels.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.network.generic.GenericReader;
import kclient.knuddels.tools.popup.Popup;
import kclient.knuddels.tools.popup.components.Panel;
import kclient.knuddels.tools.popup.components.TextPanel;
import kclient.knuddels.tools.popup.tools.Location;
import kclient.knuddels.tools.popup.tools.layout.BorderLayout;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.module.script.ScriptApp;
import kclient.module.script.ScriptModule;
import kclient.tools.Logger;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class CommandParser {
    public static boolean parse(GroupChat groupChat, String message, String channel) {
        String command = message.substring(1).split(" ")[0];
        String cmd = command.toLowerCase();
        String arg = "";
        if (message.length() > cmd.length() + 1) {
            arg = message.substring(message.indexOf(' ') + 1);
        }
        if (cmd.equals("p")) { //Custom Command implementation in Private Chat
            if (arg.contains(":")) {
                String msg = arg.substring(arg.indexOf(':') + 1);
                if (msg.charAt(0) == '/')
                    return CommandParser.parse(groupChat, msg, channel);
            }
        }
        
        if (cmd.equals("toolbar")) {
            groupChat.toggleToolbar();
            return true;
        } else if (cmd.equals("effect")) {
            if (arg.charAt(0) == '+' || arg.charAt(0) == '-') {
                String action = arg.charAt(0) == '+' ? "a" : "r";
                String effect = arg.substring(1).split(" ")[0];
                String nick = groupChat.getNickname();
                if (arg.length() > effect.length() + 1)
                    nick = arg.substring(arg.indexOf(' ') + 1);
                
                PacketBuilder buffer = new PacketBuilder("4");
                buffer.writeNull(); buffer.writeString(action);
                buffer.writeNull(); buffer.writeString(nick);
                buffer.writeNull(); buffer.writeString(effect);
                groupChat.receive(buffer.toString());
            }
            return true;
        } else 
        //<editor-fold defaultstate="collapsed" desc="kclient">
        if (cmd.equals("kclient")) {
            KTab tabPanel = new KTab(0, "KClient [1.0." + Start.REVISION +"] by SeBi", "°>U-Labs.de|https://u-labs.de/<°", "KClient", 
                "°>CENTER<°"
                + "°>http://knds.sebitm.info/kclient/logo.png<°#"
                + "°B°Knuddels Bot Client##°r°°>LEFT<°"
                + "Der KClient ist ein manipulierter Knuddels Client, der es ermöglicht den Client auf jede Art und Weise zu verändern und zu erweitern.##"
                + "Die Manipulation wird automatisch durchgeführt wenn man einen Login hinzufügt. Sollte der \"\"KLoader\"\" bereits eine Instanz der gewählten Applet Version "
                + "beinhalten wird diese verwendet und die Manipulation muss nicht erneut durchgeführt weden.##"
                + "Ein sehr großer Dank geht an °>patlux|https://u-labs.de/members/patlux-4/<° und °>UnReal|https://u-labs.de/members/unreal-2321/<°");
            
            for (Module mdl : groupChat.getModule()) {
                StringBuilder mdlBuffer = new StringBuilder();
                mdlBuffer.append("_Name:_ ").append(mdl.getName()).append("#");
                mdlBuffer.append("_Author:_ ").append(mdl.getAuthor()).append("#");
                mdlBuffer.append("_Version:_ ").append(mdl.getVersion()).append("#");
                mdlBuffer.append("_Beschreibung:_#").append(mdl.getDescription());
                tabPanel.newTab(mdl.getName(), (((ModuleBase)mdl).getState() ? "°>py_g.gif<°" : "°>py_r.gif<°") + mdl.getName() + "°+0600°" + (((ModuleBase)mdl).getState() ? 
                        "°BB>py_r.gif<>_hDeaktivieren|/mdl -" + mdl.getName() + "<°" : 
                        "°BB>py_g.gif<>_hAktivieren|/mdl +" + mdl.getName() + "<°") + "§", mdlBuffer.toString());
            }
            
            StringBuilder buffer = new StringBuilder();
            for (ScriptApp app : ((ScriptModule)groupChat.getModule("ScriptApi")).getApps()) {
                buffer.append("°>py_").append(app.getState() ? "g" : "r").append(".gif<° _").append(app.getName()).append("_ [").append(app.getVersion()).append("] by ").append(app.getAuthor()).append("#");
                buffer.append("°%05°").append(app.getDescription()).append("#");
                buffer.append("°BB>_hDeaktivieren|/mdl scriptapi -").append(app.getName()).append("<° | ");
                buffer.append("°BB>_hAktivieren|/mdl scriptapi +").append(app.getName()).append("<° | ");
                buffer.append("°BB>_hNeuladen|").append("/mdl scriptapi r").append(app.getName()).append("<°#°r°§°-°");
            }
            
            tabPanel.newTab("Apps", "Apps [°BB>_hReload|/mdl scriptapi reload<°]", buffer.toString());
            groupChat.receive(Popup.create("KClient", null, tabPanel.getSwitchTab(), 770, 570, false));
            return true;
        } else 
        //</editor-fold>
        //<editor-fold defaultstate="collapsed" desc="mdl">
        if (cmd.equals("mdl")) {
            if (arg.charAt(0) == '+') {
                String mdlName = arg.substring(1);
                Module mdl = groupChat.getModule(mdlName);
                if (mdl == null) {
                    groupChat.printBotMessage(channel, "Das Module _" + mdlName + "_ existiert nicht!");
                } else {
                    if (((ModuleBase)mdl).getState()) {
                        groupChat.printBotMessage(channel, "Das Module _" + mdl.getName() + "_ ist bereits aktiv!");
                    } else {
                        groupChat.printBotMessage(channel, "Das Module _" + mdl.getName() + "_ wurde aktiviert.");
                        ((ModuleBase)mdl).setState(true);
                    }
                }
            } else if (arg.charAt(0) == '-') {
                String mdlName = arg.substring(1);
                Module mdl = groupChat.getModule(mdlName);
                if (mdl == null) {
                    groupChat.printBotMessage(channel, "Das Module _" + mdlName + "_ existiert nicht!");
                } else {
                    if (!((ModuleBase)mdl).getState()) {
                        groupChat.printBotMessage(channel, "Das Module _" + mdl.getName() + "_ ist nicht aktiv!");
                    } else {
                        groupChat.printBotMessage(channel, "Das Module _" + mdl.getName() + "_ wurde deaktiviert.");
                        ((ModuleBase)mdl).setState(false);
                    }
                }
            } else if (arg.charAt(0) == 'r') {
                String mdlName = arg.substring(1);
                Module mdl = groupChat.getModule(mdlName);
                if (mdl == null) {
                    groupChat.printBotMessage(channel, "Das Module _" + mdlName + "_ existiert nicht!");
                } else {
                    boolean cState = ((ModuleBase)mdl).getState();
                    //mdl.save();
                    ((ModuleBase)mdl).setState(false);
                    mdl.load();
                    ((ModuleBase)mdl).setState(cState);
                    groupChat.printBotMessage(channel, "Das Module _" + mdl.getName() + "_ wurde neugeladen!");
                }
            } else if (arg.charAt(0) == '?') {
                String mdlName = arg.substring(1);
                Module mdl = groupChat.getModule(mdlName);
                if (mdl == null) {
                    groupChat.printBotMessage(channel, "Das Module _" + mdlName + "_ existiert nicht!");
                } else {
                    groupChat.printBotMessage(channel, "_Beschriebung (" + mdl.getName() + ")_:#" + mdl.getDescription());
                }
            } else {
                String arg2 = "";
                if (arg.split(" ").length > 1) {
                    arg2 = arg.substring(arg.indexOf(' ') + 1);
                    arg = arg.split(" ")[0].toLowerCase();
                } else {
                    groupChat.printBotMessage(channel, "_Verwendung:_ /mdl MODULE__NAME COMMAND PARAMS");
                    return true;
                }

                boolean exists = false;
                for (Module mdl : groupChat.getModule()) {
                    if (arg.toLowerCase().equals(mdl.getName().toLowerCase())) {
                        if (mdl.handleCommand(arg, arg2, channel)) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists)
                    groupChat.printBotMessage(channel, "Das Module _" + arg + "_ existiert nicht!");
            }
            groupChat.refreshToolbar(channel);
            return true;
        } else 
        //</editor-fold>
        if (cmd.equals("logger")) {
            if (arg.isEmpty()) {
                groupChat.printBotMessage(channel, "LogLevel angeben! _Beispiel_: /logger [Error, Info, Debug, All, None]");
            } else {
                try {
                    Logger.Level level = Logger.Level.valueOf(arg.toUpperCase());
                    Logger.get().setLevel(level);
                    groupChat.printBotMessage(channel, "LogLevel auf _" + level.name() + "_ gesetzt!");
                } catch (IllegalArgumentException e) {
                    Logger.get().error(e);
                    groupChat.printBotMessage(channel, "LogLevel angeben! _Beispiel_: /logger [Error, Info, Debug, All, None]");
                }
            }
            return true;
        } else if (cmd.equals("sendpublic")) {
            if (arg.contains(":")) {
                String toChannel = arg.substring(0, arg.indexOf(':'));
                String sendMessage = arg.substring(toChannel.length() + 1);
                groupChat.sendPublic(toChannel, sendMessage);
            }
            groupChat.refreshToolbar(channel);
            return true;
        } else 
        //<editor-fold defaultstate="collapsed" desc="sendrnd">
        if (cmd.equals("sendrnd")) {
            StringBuilder buffer = new StringBuilder();
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(new FileInputStream("data" + File.separator + "rndText.txt"), "UTF-8"));
                String line;
                while ((line = reader.readLine()) != null)
                    buffer.append(line).append("\n");
            } catch (IOException e) {
                Logger.get().error(e);
            } finally {
                if (reader != null)
                    try {
                        reader.close();
                    } catch (IOException e) {
                    }
            }
            String[] args = arg.split(" ");
            if (args.length == 1) {
                try {
                    int length = Integer.parseInt(args[0]);
                    if (buffer.length() < length) {
                        return true;
                    }
                    String rndText = buffer.substring(0, length);
                    groupChat.sendPublic(channel, rndText);
                } catch (Exception e) {
                }
            } else if (args.length == 2) {
                try {
                    int length = Integer.parseInt(args[0]);
                    int wdh = Integer.parseInt(args[1]);
                    if (buffer.length() < length) {
                        return true;
                    }
                    String rndText = buffer.substring(0, length);
                    for (int i = 0; i < wdh; i++) {
                        groupChat.sendPublicDelay(channel, rndText, i * 5000);
                    }
                } catch (NumberFormatException e) {
                }
            } else if (args.length == 3) {
                try {
                    int length = Integer.parseInt(args[0]);
                    int wdh = Integer.parseInt(args[1]);
                    int sleep = Integer.parseInt(args[2]);
                    if (buffer.length() < length) {
                        return true;
                    }
                    String rndText = buffer.substring(0, length);
                    for (int i = 0; i < wdh; i++) {
                        groupChat.sendPublicDelay(channel, rndText, i * sleep);
                    }
                } catch (NumberFormatException e) {
                }
            } else if (args.length == 5) {
                try {
                    int length = Integer.parseInt(args[0]);
                    int wdh = Integer.parseInt(args[1]);
                    int sleep = Integer.parseInt(args[2]);
                    String text = arg.substring(args[0].length() + args[1].length() + args[2].length() + 2);
                    if (buffer.length() < length) {
                        return true;
                    }
                    String rndText = buffer.substring(0, length);
                    for (int i = 0; i < wdh; i++) {
                        groupChat.sendPublicDelay(channel, text + rndText, i * sleep);
                    }
                } catch (NumberFormatException e) {
                }
            } else {
                groupChat.printBotMessage(channel, "_Verwendung:_#/sendrnd LENGTH#/sendrnd LENGTH WDH#/sendrnd LENGTH WDH SLEEP#/sendrnd LENGTH WDH SLEEP TEXT");
            }
            return true;
        }
        //</editor-fold>
        return false;
    }
}
