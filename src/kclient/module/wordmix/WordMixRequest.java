package kclient.module.wordmix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 *
 * @author SeBi
 */
public class WordMixRequest {
    private final String url, params;
    
    public WordMixRequest(String url, String params) {
        this.url = url;
        this.params = params;
    }
    
    public String make() {
        StringBuilder buffer = new StringBuilder();
        try {
            URL requestUrl = new URL(this.url + "?" + this.params);
            URLConnection con = requestUrl.openConnection();
            con.setConnectTimeout(3000);
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
