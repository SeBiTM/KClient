package kclient.module.wordmix.engines;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import kclient.module.wordmix.WordMixBot;
import kclient.module.wordmix.WordMixProcess;
import kclient.module.wordmix.WordMixRequest;
/**
 *
 * @author SeBi
 */
public class GoogleEngine implements WordMixEngine {
    private final WordMixProcess process;
    
    public GoogleEngine(WordMixProcess p) {
        this.process = p;
    }
    
    @Override
    public void getAnswer(String mix) {
        String source = this.makeRequest(mix);
        for (String s : source.split("<b>")) {
            String[] t = s.split("</b>");
            if (t[0].split(" ").length == mix.split(" ").length){
                if (!WordMixBot.replace(t[0]).equalsIgnoreCase(WordMixBot.replace(mix))) {
                    this.process.setAnswer(t[0], this);
                    break;
                }
            }
        }
        for(String s : source.split("<em>")) {
            String[] t = s.split("</em>");
            if (t[0].split(" ").length == mix.split(" ").length) {
                if (!WordMixBot.replace(t[0]).equalsIgnoreCase(WordMixBot.replace(mix))) {
                    this.process.setAnswer(t[0], this);
                    break;
                }
            }
        }
        
        String[] data = source.split("<b>");
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String word = data[i].split("</b>")[0];
            if (mix.contains("\"" + word + "\"")) {
                buffer.append(word).append(" ");
                if (WordMixBot.replace(buffer.toString()).length() >= WordMixBot.replace(mix).length()) {
                    buffer.delete(buffer.length() - 1, buffer.length());
                    this.process.setAnswer(buffer.toString(), this);
                    break;
                }
            }
        }
        
        data = source.split("<em>");
        buffer = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            String word = data[i].split("</em>")[0];
            if (mix.contains("\"" + word + "\"")) {
                buffer.append(word).append(" ");
                if (WordMixBot.replace(buffer.toString()).length() >= WordMixBot.replace(mix).length()) {
                    buffer.delete(buffer.length() - 1, buffer.length());
                    this.process.setAnswer(buffer.toString(), this);
                    break;
                }
            }
        }
        
        this.process.setAnswer(null, this);
    }
    
    private String makeRequest(String mix) {
        try {
            WordMixRequest r = new WordMixRequest("http://www.google.de/search", 
                    "hl=&q=" + URLEncoder.encode(mix, "UTF-8") + "&btnG=Google-Suche&meta=");
            return r.make();
        } catch (UnsupportedEncodingException ex) {
        }
        return null;
    }
}
