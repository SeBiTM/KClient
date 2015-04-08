package kclient.knuddels.tools;

import kclient.knuddels.GroupChat;
import kclient.module.Module;

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
            if (arg.charAt(0) == '/')
                return CommandParser.parse(groupChat, arg, channel);
        }
        
        if (cmd.equals("mdl")) {
            for (Module mdl : groupChat.getModule()) {
                String arg2 = "";
                if (arg.split(" ").length > 1)
                    arg2 = arg.substring(arg.indexOf(' ') + 1);
                else {
                    groupChat.printBotMessage(channel, "_Verwendung:_ /mdl MODULE_NAME COMMAND PARAMS#PARAMS = optional");
                    return true;
                }
                if (arg.equals(mdl.getName().toLowerCase())) {
                    if (mdl.handleCommand(arg, arg2, channel))
                        return true;
                }
            }
        }
        return false;
    }
}
