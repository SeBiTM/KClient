package kclient.module.quiz;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 *
 * @author SeBi
 */
public class QuizDatabase {
    private final String name, url, get, add, count;
    
    public QuizDatabase(String name, String url, String get, String add, String count) {
        this.url = url;
        this.name = name;
        this.get = get;
        this.add = add;
        this.count = count;
    }
    
    public String getName() {
        return this.name;
    }
    
    public void getAnswer(String quest, QuizProcess process) {
        if (this.get == null)
            return;
        try {
            String answer = request(this.get.replace("{quest}", URLEncoder.encode(quest, "UTF-8")));
            if (answer.isEmpty())
                process.setAnswer(null, true);
            else {
                process.setAnswer(answer, true);
            }
        } catch (UnsupportedEncodingException ex) {
            process.setAnswer(null, true);
        }
    }
    
    public void add(String quest, String answer) {
        if (this.add == null)
            return;
        try {
            request(this.add
                    .replace("{quest}", URLEncoder.encode(quest, "UTF-8"))
                    .replace("{answer}", URLEncoder.encode(answer, "UTF-8"))
            );
        } catch (UnsupportedEncodingException ex) {
        }
    }
    
    public int count() {
        try {
            return Integer.parseInt(request(this.count));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private String request(String params) {
        StringBuilder buffer = new StringBuilder();
        try {
            URL requestUrl = new URL(this.url + "?" + params);
            URLConnection con = requestUrl.openConnection();
            con.setConnectTimeout(10000);
            con.setRequestProperty("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            con.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 6.0; de; rv:1.9.2.3) Gecko/20100401 Firefox/3.6.3");
            con.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.7");
            con.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
            String decodedString;
            while ((decodedString = in.readLine()) != null)
                buffer.append(decodedString);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }
}
