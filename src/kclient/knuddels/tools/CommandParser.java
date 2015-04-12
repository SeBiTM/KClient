package kclient.knuddels.tools;

import kclient.knuddels.GroupChat;
import kclient.knuddels.tools.popup.Popup;
import kclient.knuddels.tools.popup.components.Panel;
import kclient.knuddels.tools.popup.components.TextPanel;
import kclient.knuddels.tools.popup.tools.Location;
import kclient.knuddels.tools.popup.tools.layout.BorderLayout;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.tools.Logger;

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
        } else if ((cmd.equals("w") && arg.equalsIgnoreCase("kclient")) || cmd.equals("kclient")) {
            KTab tabPanel = new KTab(0, "KClient", "°>U-Labs.de|https://u-labs.de/<°", "KClient", 
                "°>CENTER<°"
                + "°>http://knds.sebitm.info/kclient/logo.png<°#"
                + "°B°Knuddels Bot Client##°r°°>LEFT<°"
                + "Der KClient ist ein manipulierter Knuddels Client, der es ermöglicht den Client auf jede Art und Weise zu verändern und zu erweitern.##"
                + "Die Manipulation wird automatisch durchgeführt wenn man einen Login hinzufügt. Sollte der \"\"KLoader\"\" bereits eine Instanz der gewählten Applet Version"
                + "beinhalten wird diese verwendet und die Manipulation muss nicht erneut durchgeführt weden.#"
                + "");
            
            for (Module mdl : groupChat.getModule()) {
                tabPanel.newTab(mdl.getName(), mdl.getName(), mdl.getDescription());
            }
            groupChat.receive(Popup.create("KClient", null, tabPanel.getSwitchTab(), 690, 560, false));
            return true;
        } else if (cmd.equals("mdl")) {
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

                for (Module mdl : groupChat.getModule()) {
                    if (arg.toLowerCase().equals(mdl.getName().toLowerCase())) {
                        if (mdl.handleCommand(arg, arg2, channel))
                            break;
                    }
                }
            }
            groupChat.refreshToolbar(channel);
            return true;
        } else if (cmd.equals("logger")) {
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
        }
        return false;
    }
}
