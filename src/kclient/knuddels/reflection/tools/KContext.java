package kclient.knuddels.reflection.tools;

import java.applet.Applet;
import java.applet.AppletContext;
import java.applet.AudioClip;
import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Iterator;
import kclient.tools.Logger;

/**
 *
 * @author SeBi
 */

public class KContext implements AppletContext {
    public Applet getApplet(String paramString) {
            return null;
    }

    public Enumeration<Applet> getApplets() {
            return null;
    }

    public AudioClip getAudioClip(URL paramURL) {
            return null;
    }

    public Image getImage(URL paramURL) {
        return Toolkit.getDefaultToolkit().getImage(paramURL);
    }

    public InputStream getStream(String paramString) {
            return null;
    }

    public Iterator<String> getStreamKeys() {
            return null;
    }

    public void setStream(String paramString, InputStream paramInputStream) {
    }

    public void showDocument(URL paramURL) {
            showDocument(paramURL, null);
    }

    public void showDocument(URL paramURL, String paramString) {
        if (Desktop.isDesktopSupported()) {
            Desktop localDesktop = Desktop.getDesktop();

            if (localDesktop.isSupported(Desktop.Action.BROWSE))
                try {
                    localDesktop.browse(paramURL.toURI());
                } catch (Exception e) {
                    Logger.get().error(e.toString());
                }
        }
    }

    public void showStatus(String paramString) {
    }
}
