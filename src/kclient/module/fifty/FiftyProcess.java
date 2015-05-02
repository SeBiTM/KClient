package kclient.module.fifty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import kclient.knuddels.tools.toolbar.Button;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class FiftyProcess {
    private final FiftyBot bot;
    private final String channel;
    private String number;
    private double tmp_risk, goalNumber;
    
    public FiftyProcess(String channel, FiftyBot bot) {
        this.bot = bot;
        this.channel = channel;
    }
    
    public void processInput(String packet, String[] tokens) {
        if (tokens[0].equals("t")) {
            if (tokens[1].equals(this.bot.getGroupChat().getNickname())) {
                if (tokens[3].contains("würfelt"))
                    this.bot.getGroupChat().refreshToolbar(channel);
            }
        } else if (tokens[0].equals("e")) {
            if (tokens[1].equals(this.bot.getGroupChat().getButlerName())) {
                String msg = tokens[3];
                if (msg.contains("°20RR°Achtung, °>sm_runde")) {
                    int index = msg.indexOf("##Wer über °RR20°_- ");
                    if (index > 0) {
                        this.number = msg.substring(index + 20, msg.indexOf(" ", index + 20));
                        this.goalNumber = Double.parseDouble(this.number);
                        this.tmp_risk = this.bot.getRisk();
                        if (this.goalNumber > 22)
                            this.tmp_risk = (this.bot.getRisk() - 0.10000000000000001D);
                        else if (this.goalNumber < 9)
                            this.tmp_risk = (this.bot.getRisk() + 0.10000000000000001D);
                        this.tmp_risk = (this.tmp_risk * this.goalNumber);
                     }
                } else if (msg.contains("Fifty! endet")) {
                    this.bot.getGroupChat().refreshToolbar(channel, new Button("Neue Runde starten", "/sendpublic " + channel + ":" + tokens[1].toLowerCase() + " fifty", true));
                } else if (msg.contains(this.bot.getGroupChat().getNickname()) && msg.contains("ist an der Reihe...")) {
                    this.number = tokens[3].split("_")[5].split(" ")[2];
                    this.goalNumber = Double.parseDouble(this.number);
                    this.tmp_risk = this.bot.getRisk();
                    if (this.goalNumber > 22)
                        this.tmp_risk = (this.bot.getRisk() - 0.10000000000000001D);
                    else if (this.goalNumber < 9)
                        this.tmp_risk = (this.bot.getRisk() + 0.10000000000000001D);
                    this.tmp_risk = (this.tmp_risk * this.goalNumber);
                    
                    int sleep = Util.rnd(7000, 15000);
                    String tmpCalc = calc(Arrays.asList(4.0,4.0,4.0,6.0,6.0,8.0,8.0,10.0,12.0,20.0), this.goalNumber, tmp_risk);
                    this.bot.getGroupChat().refreshToolbar(tokens[2], new Button("Steche " + tmpCalc + " in " + (sleep/1000) + " Sekunden..."));
                    this.bot.getGroupChat().sendPublicDelay(channel, tmpCalc, sleep);
                }
            }
        } else if (tokens[0].equals("r")) {
            if (tokens[1].equals(this.bot.getGroupChat().getButlerName())) {
                String msg = tokens[4];
                if (msg.startsWith("Deine Würfel:")) {
                    int index = msg.indexOf("_");
                    if (index > 0) {
                        String w = msg.substring(index + 1, msg.indexOf("_", index + 1));
                        List<Double> dices = this.parseDices(w);
                        String dice = this.calc(dices, this.goalNumber, this.tmp_risk);
                        int sleep = Util.rnd(6000, 12000);
                        this.bot.getGroupChat().refreshToolbar(this.channel, new Button("Sende " + dice + " in " + (sleep/1000) + " Sekunden..."));
                        this.bot.getGroupChat().sendPublicDelay(this.channel, dice, sleep);
                    }
                } else if (msg.contains("Du bist nun ")) {
                    this.bot.getGroupChat().refreshToolbar(channel);
                }
            }
        }
    }
    
    private String calc(List<Double> dices, double goal, double risk) {
        StringBuilder buffer = new StringBuilder("/d ");
        double goalNum = 0;
        Collections.shuffle(dices);
        
        List<Double> use = new ArrayList<>();
        for (double dice : dices) {
            double tmp = dice;
            tmp += goalNum;
            if (tmp < (goal + 1 + risk)) {
                buffer.append(Double.toString(dice).split("\\.")[0]).append("+");
                use.add(dice);
                goalNum += dice;
            }
        }
        
        buffer.delete(buffer.length() - 1, buffer.length());
        return buffer.toString();
    }
    private List<Double> parseDices(String str) {
        List<Double> dices = new ArrayList<>();
        String[] tmp = str.split(" \\+ ");
        for (String t : tmp) {
            String[] p = t.split("W");
            try {
                int count = p[0].isEmpty() ? 1 : Integer.parseInt(p[0]);
                double num = Double.parseDouble(p[1]);
                for (int i = 0; i < count; i++)
                    dices.add(num);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return dices;
    }
}
