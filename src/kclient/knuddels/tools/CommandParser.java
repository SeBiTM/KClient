package kclient.knuddels.tools;

import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.tools.popup.Popup;
import kclient.knuddels.tools.popup.components.Panel;
import kclient.knuddels.tools.popup.components.TextPanel;
import kclient.knuddels.tools.popup.tools.Location;
import kclient.knuddels.tools.popup.tools.layout.BorderLayout;
import kclient.module.Module;
import kclient.module.ModuleBase;
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
        
        if (cmd.equals("sendrnd")) {
            int length = Integer.parseInt(arg);
            String wiki = "Das Stammesgebiet der Yankton und Yanktonai erstreckte sich über die Präriegebiete des heutigen North und South Dakota, des nordwestlichen Iowa und südwestlichen Minnesota.\n" +
            "Früher wurden unter dem falschen Sammelbegriff Nakota neben den Westlichen Dakȟóta (Yankton und Yanktonai) auch die sprachlich verwandten Assiniboine (Nakhóta, Nakhóda oder Nakhóna) sowie Stoney (Nakhóda oder Nakhóta, eine Assiniboine-Splittergruppe) gezählt - jedoch hatten sich Letztere bereits 1640 von der Wazikute (‘Shooters Among the Pines’) Band der Upper Yanktonai abgespaltet und waren größtenteils nach Norden auf die Prärieprovinzen von Kanada westlich des Lake Winnipeg gezogen. Dort bildeten sie die Cree-Assiniboine (Nehiyaw-Pwat) oder Iron Confederacy, eine mächtige Militär-sowie Handels-Konföderation der dominierenden Woodland und Plains Cree sowie der Assiniboine, Stoney und später der Manitoba Saulteaux und Westliche Saulteaux (Plains Ojibwe) (den Erzfeinden der Sioux-Völker), was nun zu immer wieder auftretenden Kämpfen und Konflikten zwischen Sioux und Assiniboine führte. Die Sioux bezeichneten die nun feindlichen Assiniboine daher als Hohe („Rebellen“).\n" +
            "Die Plains Assiniboine (Southern Assiniboine) dominierten Mitte des 18. Jahrhunderts bis Anfang des 19. Jahrhunderts die kanadischen Prärieprovinzen im Südosten Saskatchewans, Südwesten Manitobas sowie im Osten Albertas, die Täler des Saskatchewan Rivers und des Assiniboine Rivers sowie im Süden die Great Plains bis zum Milk River und Missouri River im Nordosten Montanas und Nordwesten North Dakotas in den USA. Im Norden streiften und wanderten die Woodland Assiniboine (Northern Assiniboine) in den Steppen und Wald- und Seengebiete der borealen Wälder entlang des Athabasca, McLeod und North Saskatchewan River.\n" +
            "Mitte des 18. Jahrhunderts trennten sich die nordwestlichsten Bands der Assiniboine von diesen, und entwickelten zusammen mit zugezogenen Lakota eine neue Stammesidentität als Stoney (auch als Stoney Nakoda Nation oder Lyärhe Nakoda bezeichnet), blieben jedoch Mitglied der Cree-Assiniboine-Allianz. Einige Stoney behaupten daher, sie verstünden die Lakota besser als die benachbarten Assiniboine und bezeichnen sich als Rocky Mountain Sioux.[3] Da die Assiniboine oft Stone oder Rocky Sioux genannt wurden, manche der Stoney sich iyarhe Nakodabi - ‘Rocky Mountain Sioux’ nannten [4]und beide sich als Nakhóda bezeichneten, wurden sie oft verwechselt oder gar als ein Volk behandelt.";
            
            System.err.println(wiki.length());
            String str = wiki.substring(0, length);
            groupChat.sendPublic(channel, str);
        }
        
        if (cmd.equals("toolbar")) {
            groupChat.toggleToolbar();
        } else if ((cmd.equals("w") && arg.equalsIgnoreCase("kclient")) || cmd.equals("kclient")) {
            KTab tabPanel = new KTab(0, "KClient [1.0." + Start.REVISION +"] by SeBi", "°>U-Labs.de|https://u-labs.de/<°", "KClient", 
                "°>CENTER<°"
                + "°>http://knds.sebitm.info/kclient/logo.png<°#"
                + "°B°Knuddels Bot Client##°r°°>LEFT<°"
                + "Der KClient ist ein manipulierter Knuddels Client, der es ermöglicht den Client auf jede Art und Weise zu verändern und zu erweitern.##"
                + "Die Manipulation wird automatisch durchgeführt wenn man einen Login hinzufügt. Sollte der \"\"KLoader\"\" bereits eine Instanz der gewählten Applet Version"
                + "beinhalten wird diese verwendet und die Manipulation muss nicht erneut durchgeführt weden.#"
                + "");
            
            for (Module mdl : groupChat.getModule()) {
                StringBuilder mdlBuffer = new StringBuilder();
                mdlBuffer.append("_Name:_ ").append(mdl.getName()).append("#");
                mdlBuffer.append("_Author:_ ").append(mdl.getAuthor()).append("#");
                mdlBuffer.append("_Version:_ ").append(mdl.getVersion()).append("#");
                mdlBuffer.append("_Beschreibung:_#").append(mdl.getDescription());
                tabPanel.newTab(mdl.getName(), mdl.getName(), mdlBuffer.toString());
            }
            groupChat.receive(Popup.create("KClient", null, tabPanel.getSwitchTab(), 750, 560, false));
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
