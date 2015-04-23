package kclient.knuddels.tools;

import kclient.knuddels.reflection.tools.ManipulationData;
import java.io.File;
import java.net.HttpURLConnection;
import java.net.URL;
import kclient.tools.Logger;
import kclient.tools.Parameter;

/**
 *
 * @author SeBi
 */
public enum ChatSystem {
    DE("Knuddels.de", "http://www.knuddels.de/applet.jnlp"),
    AT("Knuddels.at", "http://www.knuddels.at/applet.jnlp"),
    COM("Knuddels.com", "http://knuddels.com/");
    
    private final String name;
    private final String version;
    private final String current_version;
    private final ManipulationData maniplation;
    private ChatSystem(String name, String site_version) {
        this.name = name;
        Parameter params = new Parameter(
            "chatsystem" + File.separator + 
            this.name().toLowerCase());
        
        this.current_version = this.getVersion(site_version);
        this.version = params.get("version");
        this.maniplation = new ManipulationData(
            "manipulator" + File.separator + 
            this.name().toLowerCase());
        
        params.close();
        Logger.get().info("   " + toString() + " initialized");
    }
    
    public String getName() {
        return this.name;
    }
    public String getVersion() {
        return this.version;
    }
    public String getCurrentVersion() {
        return this.current_version == null ? "n/a" : this.current_version;
    }
    public ManipulationData getManipulation() {
        return this.maniplation;
    }
    
    private String getVersion(String site) {
        try {
            URL url = new URL(site);
            HttpURLConnection con = (HttpURLConnection)url.openConnection();
            byte[] buffer = new byte[con.getContentLength()];
            int pos = 0;
            while (pos < buffer.length)
                    pos += con.getInputStream().read(buffer, pos, buffer.length - pos);

            String content = new String(buffer);
            int ind = content.indexOf("appVersion");
            if (ind > 0) {
                content = content.substring(ind + 24);
            } else {
                ind = content.indexOf("<jar href=\"knuddels");
                if (ind > 0) {
                    content = content.substring(ind + 19);
                } else {
                    ind = content.indexOf("?v=");
                    if (ind < 0) {
                        ind = content.indexOf("vn='");
                        if (ind > 0)
                            content = content.substring(ind + 4);
                        else {
                            ind = content.indexOf("archive=\"knuddels");
                            content = content.substring(ind + 17);
                        }
                    }
                }
            }
            String v = content.substring(0, 6);
            return (v.startsWith("k") || v.startsWith("m")) ? v : getVersion();
        } catch (Exception e) {
            Logger.get().error(e);
        }
        return null;
    }
    
    public static ChatSystem fromName(String name) {
        for (ChatSystem s : ChatSystem.values())
            if (s.getName().equals(name))
                return s;
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("[ChatSystem - %s, Version: %s, CurrentVersion: %s]", getName(), getVersion(), getCurrentVersion());
    }
}
