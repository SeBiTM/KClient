package kclient.module.quiz;

import java.io.File;
import java.io.FileReader;
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
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class QuizBot extends ModuleBase implements Module {
    private boolean autoJoin;
    private final List<QuizDatabase> databases;
    private final Map<String, QuizProcess> processes;
    
    public QuizBot(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
        this.autoJoin = true;
        this.databases = new ArrayList<>();
        this.processes = new HashMap<>();
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        if (tokens[0].equals("r")) {
            //r\0James\0xSx\0Quiz\0Es ist so weit, ein neues Quizturnier beginnt genau jetzt. Alle, die jetzt mitspielen wollen, geben einfach _°BB>/ok 866372|"<r°_ ein.\0 \0 
            if (tokens[4].contains("Es ist so weit,") && this.autoJoin) {
                int index = tokens[4].indexOf("_°BB>/ok ");
                if (index > 0) {
                    String zahl = tokens[4].substring(index + "_°BB>/ok ".length());
                    index = zahl.indexOf("|\"<r°_");
                    if (index > 0) {
                        zahl = zahl.substring(0, index);
                        this.groupChat.sendPublicDelay(tokens[3].equals("-") ? this.groupChat.getCurrentChannel() : tokens[3], String.format("/ok %s", zahl), Util.rnd(5000, 10000));
                    }
                }
            }
        } else if (tokens[0].equals("e")) {
            if (tokens[2].contains("Quiz")) {
                String msg = tokens[3];
                if (tokens[1].equals(this.groupChat.getButlerName())) {
                    if (msg.startsWith("°B18°_")) {
                        String quest = msg.substring(6);
                        int index = quest.indexOf("_°r°");
                        if (index > 0) {
                            quest = quest.substring(0, index);
                            if (this.processes.containsKey(tokens[2])) {
                                this.processes.get(tokens[2]).stop();
                                this.processes.remove(tokens[2]);
                            }
                            this.processes.put(tokens[2], QuizProcess.start(this, this.groupChat, tokens[2], quest));
                        }
                    } else if (msg.contains("richtige Lösung") || 
                               msg.contains("richtige Antwort") ||
                               msg.contains("Zeit vorbei")) 
                    {
                        int index = msg.indexOf("°R18°_");
                        if (index > 0) {
                            String answer = msg.substring(index + 6);
                            index = answer.indexOf("_°r°");
                            if (index > 0) {
                                answer = answer.substring(0, index);
                                if (this.processes.containsKey(tokens[2])) {
                                    this.processes.get(tokens[2]).setAnswer(answer, false);
                                    this.processes.remove(tokens[2]);
                                }
                                this.groupChat.refreshToolbar(tokens[2]);
                            }
                        }
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
    public GenericProtocol handleExtendOutput(GameConnection connection, GenericProtocol module) {
        return module;
    }
    @Override
    public GenericProtocol handleExtendInput(GameConnection connection, GenericProtocol module) {
        return module;
    }

    public List<QuizDatabase> getDatabases() {
        return this.databases;
    }
    @Override
    public String getName() {
        return "Quiz";
    }
    @Override
    public String getAuthor() {
        return "°>_hSeBi|https://u-labs.de/members/sebi-2841/<°";
    }
    @Override
    public String getDescription() {
        return "Das Quiz Module hilft die im Spiel Quiz mit einer von dir defenierten Datenbank.#"
                + "Datenbanken können in der Datei \"data/module/quiz/database.properties\" eingestellt werden.##"
                + "°>bullet2.png<° Meldet sich auf Wunsch automatisch an#"
                + "°>bullet2.png<° Antwort wird in der Toolbar angezeigt, und wird bei Klick in Kleinbuchstaben gesendet#"
                + "°>bullet2.png<° Nicht gefundene Lösungen werden zur Datenabnk geschickt";
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
    }

    @Override
    public List<Button> getButtons(String channel) {
        if (channel.contains("Quiz")) {
            return Arrays.asList(new Button[] {
                new Button("Autojoin", "py_" + (this.autoJoin?"g":"r")+".gif", "/mdl " + getName() + " autojoin:" + (this.autoJoin ? "false":"true"), false),
                new Button("Quiz", "py_" + (this.state ? "g" : "r") + ".gif", "/mdl " + (this.state ? "-" : "+") + getName(), false)
            });
        }
        return null;
    }
    @Override
    public boolean handleCommand(String cmd, String arg, String channel) {
        String[] args = arg.split(":");
        if (args[0].equalsIgnoreCase("autojoin")) {
            this.autoJoin = Boolean.parseBoolean(args[1]);
            this.groupChat.printBotMessage(channel, String.format("Quiz Autojoin gesetzt (%s)", this.autoJoin));
        }
        this.groupChat.refreshToolbar(channel);
        return true;
    }

    @Override
    public void save() {
    }

    @Override
    public void load() {
        Properties props = new Properties();
        FileReader reader = null;
        try {
            reader = new FileReader("data" + File.separator + "module" + File.separator + "quiz" + File.separator + "database.properties");
            props.load(reader);
        } catch (IOException e) {
            Logger.get().error(e);
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (IOException e) {
                }
        }
        for (int i = 1; i < 10; i++) {
            String name = props.getProperty("database" + i + "_name");
            if (name == null) {
                break;
            }
            this.databases.add(new QuizDatabase(
                name,
                props.getProperty("database" + i + "_url"),
                props.getProperty("database" + i + "_search"),
                props.getProperty("database" + i + "_add"),
                props.getProperty("database" + i + "_count")
            ));
        }
        
        for (QuizDatabase db : this.databases)
            Logger.get().info("[Database] " + db.getName() + " " + db.count());
    }
}
