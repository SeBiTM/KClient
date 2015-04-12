package kclient;

import java.awt.Desktop;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import javax.swing.JOptionPane;
import kclient.tools.Logger;
import kclient.ui.ClientGui;

/**
 *
 * @author SeBi
 */
public class Start {
    public static final int REVISION = 5;
    
    public static void main(String[] args) {
        try {
            Logger.get().info("Checking Version (Client: " + Start.REVISION + ")");
            URL requestUrl = new URL("http://knds.sebitm.info/kclient/version.txt");
            URLConnection con = requestUrl.openConnection();
            con.setConnectTimeout(10000);
            con.connect();

            Properties p = new Properties();
            p.load(con.getInputStream());
            Logger.get().info(String.format(" Current Revision: %s",
                    p.getProperty("rev")));
            
            if (Integer.parseInt(p.getProperty("rev")) > Start.REVISION) {
                if (JOptionPane.showConfirmDialog(null, p.getProperty("msg"), p.getProperty("title"), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (Desktop.isDesktopSupported()) {
                        Desktop localDesktop = Desktop.getDesktop();
                        if (localDesktop.isSupported(Desktop.Action.BROWSE))
                            try {
                                localDesktop.browse(new URL(p.getProperty("url")).toURI());
                            } catch (MalformedURLException | URISyntaxException e) {
                                Logger.get().error(e);
                            }
                    }
                    System.exit(0);
                }
            }
        } catch (IOException e) {
            Logger.get().error(e);
        }
        ClientGui.main(args);
    }
    
}
