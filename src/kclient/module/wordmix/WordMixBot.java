package kclient.module.wordmix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.tools.toolbar.Button;
import kclient.module.Module;
import kclient.module.ModuleBase;


/**
 *
 * @author SeBi
 */
public class WordMixBot extends ModuleBase implements Module {
    private final Map<String, WordMixProcess> processes;
    public int rounds, found, not_found, wins;

    public WordMixBot(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
        this.processes = new HashMap<>();
    }
    
    private List<String> parseMix(String s) {
        List<String> mixxed = new ArrayList<>();
        String[] parts = s.split("\\(°B°");
        for (String string : parts) {
            if(string.contains("°°) "))
                string = string.substring(5);
            mixxed.add(string);
        }
        return mixxed;
    }
    public static String buildAnswer(String code, WordMixProcess p) {
        if (code == null)
            return null;
        Map<String, Integer> table = new HashMap();
        String[] awords = code.split(" ");
        String[] mwords = p.getMix().split(" ");
        for (String word : mwords) {
            table.put(word, table.size() + (p.containsNull() ? 0 : 1));
        }
        StringBuilder buffer = new StringBuilder();
        List<Integer> used = new ArrayList<>();
        for (String word : awords) {
            if (!table.containsKey(word))
                return null;
            int num = table.get(word);
            if (used.contains(num))
                continue;
            used.add(num);
            buffer.append(num);
        }
        if (buffer.toString().length() == mwords.length)
            return buffer.toString();
        return null;
    }
    public static String replace(String mix) {
        return mix.replace(".", "").replace(",", "")
                .replace("?", "").replace("!", "")
                .replace("\"", "").replace(":", "")
                .replace("-", "").replace("(", "")
                .replace(")", "").replace("  ", "");
    }

    @Override
    public String getAuthor() {
        return "SeBi";
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
    }
    @Override
    public String getDescription() {
        return "...";
    }
    @Override
    public String getName() {
        return "WordMix";
    }

    @Override
    public List<Button> getButtons(String channel) {
        if (!channel.toLowerCase().contains("wordmix"))
            return null;
        return Arrays.asList(new Button[] {
            new Button("WordMix", "py_" + (this.state ? "g" : "r") + ".gif", "/mdl " + (this.state ? "-" : "+") + getName(), false)
        });
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        if (tokens[0].equals("e")) {
            if (tokens[1].equals(this.groupChat.getButlerName()) &&
                    tokens[2].toLowerCase().contains("wordmix") &&
                    tokens[3].contains("(°B°1°°)"))
            {
                this.rounds++;
                if (this.processes.containsKey(tokens[2])) {
                    this.processes.get(tokens[2]).stop();
                    this.processes.remove(tokens[2]);
                }
                String mix = tokens[3].split("#_")[1].split("_")[0];
                List<String> words = this.parseMix(mix);
                boolean containsNull = mix.contains("°°0");
                StringBuilder buffer = new StringBuilder();
                for (String m : words)
                    buffer.append(m).append(" ");
                mix = WordMixBot.replace(buffer.toString());
                buffer = new StringBuilder();
                for (String m : mix.split(" ")) {
                    buffer.append("\"").append(m).append("\" ");
                }
                this.processes.put(tokens[2], WordMixProcess.start(this, this.groupChat, tokens[2], mix, buffer.toString(), containsNull));
            } else if (tokens[1].equals(this.groupChat.getButlerName())) {
                if (tokens[2].toLowerCase().contains("wordmix")) {
                    if (tokens[3].contains("°B°_") || (tokens[3].contains("Lösung") && tokens[3].contains("_"))) 
                    {
                        this.groupChat.refreshToolbar(tokens[2], new Button("Neue Runde starten", "/sendpublic " + tokens[2] + ":mix " + this.groupChat.getButlerName().toLowerCase(), true));
                    }
                }
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
        return false;
    }

    @Override
    public void save() {
    }

    @Override
    public void load() {
    }
}
