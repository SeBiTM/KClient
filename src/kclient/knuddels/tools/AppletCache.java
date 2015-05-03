package kclient.knuddels.tools;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import kclient.tools.Logger;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class AppletCache {
    private static AppletCache def;
    
    private Map<String, AppletCache> applets;
    private List<File> fileList;
    
    static {
        def = new AppletCache();
    }
    public static AppletCache get() {
        return def;
    }
    
    private AppletCache() {
        this.applets = new HashMap<>();
        Logger.get().info("  Loading Applet Cache");
        File appDir = new File("data" + File.separator + "cache");
        if (!appDir.exists())
            appDir.mkdirs();
        checkCache(appDir);
        File[] verDirs = appDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File verDir : verDirs) {
            String appVersion = verDir.getName();
            File[] files = verDir.listFiles();
            URL[] urlList = new URL[files.length];
            for (int i = 0; i < files.length; i++) {
                try {
                    urlList[i] = files[i].toURI().toURL();
                } catch (MalformedURLException e) {
                    Logger.get().error(e);
                }
            }
            this.applets.put(appVersion, new AppletCache(appVersion, urlList, false));
        }
    }
    private AppletCache(String version, URL[] urls, boolean fromWeb) {
        this.fileList = new ArrayList<>();
        Logger.get().info("    [Cache - Version: " + version + ", FromWeb: " + fromWeb + "] initialized");
        File appDir = new File("data" + File.separator + "cache" + File.separator + version);
        if (fromWeb) {
            appDir.mkdirs();
            for (URL u : urls) {
                File appSave = new File(appDir.getPath() + File.separator + u.getFile());
                fileList.add(appSave);
                Util.downloadFile(appSave, u);
            }
        } else {
            for (URL u : urls)
                try {
                    fileList.add(new File(u.toURI()));
                } catch (URISyntaxException ex) {
                }
        }
    }
    
    public URL[] getApplets() throws MalformedURLException {
        URL[] urls = new URL[this.fileList.size()];
        int i = 0;
        for (File file : this.fileList) {
            String urlStr = file.toURI().toURL().toString() + "!/";
            urls[i++] = new URL("jar:" + urlStr);
        }
        return urls;
    }
    
    public AppletCache getCache(ChatSystem system) {
        String appVersion = system.getVersion();
        if (this.applets.containsKey(appVersion))
            return this.applets.get(appVersion);
        AppletCache appCache = new AppletCache(appVersion, Util.getApplets(system), true);
        this.applets.put(appVersion, appCache);
        return appCache;
    }
 
    private void checkCache(File appDir) {
        ChatSystem[] systems = ChatSystem.values();
        File[] verDirs = appDir.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        List<File> toRemove = new ArrayList<>();
        for (File verDir : verDirs) {
            String appVersion = verDir.getName();
            boolean rem = true;
            for (ChatSystem sys : systems) {
                if (sys.getVersion().equals(appVersion))
                    rem = false;
            }
            if (rem)
                toRemove.add(verDir);
        }
        for (File dir : toRemove) {
            File[] fList = dir.listFiles();
            for (File f : fList)
                f.delete();
            dir.delete();
        }
            
    }
}
