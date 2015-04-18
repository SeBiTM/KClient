package kclient.module.quiz;

import kclient.knuddels.GroupChat;
import kclient.knuddels.tools.toolbar.Button;

/**
 *
 * @author SeBi
 */
public class QuizProcess {
    private final GroupChat groupChat;
    private final QuizBot bot;
    private boolean running;
    private final String channel, quest;
    private int failCount;
    
    public QuizProcess(QuizBot bot, GroupChat groupChat, String quest, String channel) {
        this.groupChat = groupChat;
        this.channel = channel;
        this.bot = bot;
        this.quest = quest;
        this.running = true;
    }
    
    public static QuizProcess start(QuizBot bot, GroupChat groupChat, String channel, final String quest) {
        final QuizProcess process = new QuizProcess(bot, groupChat, quest, channel);
        groupChat.refreshToolbar(channel, new Button("Suche..."));
        for (final QuizDatabase db : bot.getDatabases()) {
            new Thread("QuizDatabase") {
                @Override
                public void run() {
                    db.getAnswer(quest, process);
                }
            }.start();
        }
        return process;
    }
    
    public void setAnswer(final String answer, boolean fromDB) {
        if (!fromDB && !answer.isEmpty()) {
            for (final QuizDatabase db : bot.getDatabases()) {
                new Thread("QuizDatabase") {
                    @Override
                    public void run() {
                        db.add(QuizProcess.this.quest, answer);
                    }
                }.start();
            }
            return;
        }
        if (!this.running)
            return;
        
        if (answer == null || answer.isEmpty()) {
            this.failCount++;
            if (this.failCount >= this.bot.getDatabases().size()) {
                this.groupChat.refreshToolbar(this.channel, new Button("Keine Antwort gefunden"));
                this.stop();
            }
            return;
        }
        if (this.running) {
            //this.groupChat.print(this.channel, String.format("°>{button} %s||call|/a %s|width|150|height|30<°", answer, answer));
            this.groupChat.refreshToolbar(this.channel, new Button(answer, String.format("/sendpublic %s:%s", channel, answer.toLowerCase())));
            this.stop();
        }
    }
    
    public void stop() {
        this.running = false;
    }
}
