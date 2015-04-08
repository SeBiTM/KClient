package kclient.module.script;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.tools.toolbar.Button;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.tools.Parameter;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class ScriptModule extends ModuleBase implements Module {
    private Map<String, ScriptApp> apps;
    
    public ScriptModule(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
        this.apps = new HashMap<>();
    }

    @Override
    public String getName() {
        return "ScriptModule";
    }
    @Override
    public String getAuthor() {
        return "SeBi";
    }
    @Override
    public String getDescription() {
        return "";
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
    }

    @Override
    public List<Button> getButtons(String channel) {
        return Arrays.asList(new Button[] {
            new Button(getName(), "/mdl " + (this.state ? "-" : "+") + getName(), "py_" + (this.state ? "g" : "r") + ".gif", false)
        });
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        if (!this.getState())
            return packet;
        
        //ToDo: AntiAdmin für User crap implemetieren
        //TODO: Parser für die App Hooks
        
        for (ScriptApp app : this.apps.values())
            packet = app.onPacketReceived(packet);
        return packet;
    }
    @Override
    public String handleOutput(String packet, String[] tokens) {
        if (!this.getState())
            return packet;
        
        for (ScriptApp app : this.apps.values())
            packet = app.onPacketSent(packet);
        return packet;
    }

    @Override
    public GenericProtocol handleExtendInput(GameConnection connection, GenericProtocol protocol) {
        if (!this.getState())
            return protocol;
        
        for (ScriptApp app : this.apps.values())
            protocol = app.onNodeReceived(connection, protocol);
        return protocol;
    }
    @Override
    public GenericProtocol handleExtendOutput(GameConnection connection, GenericProtocol protocol) {
        if (!this.getState())
            return protocol;
        
        for (ScriptApp app : this.apps.values())
            protocol = app.onNodeSent(connection, protocol);
        return protocol;
    }

    @Override
    public boolean handleCommand(String cmd, String arg, String channel) {
        if (arg.equals("reload")) {
            this.load();
            return true;
        }
        boolean cmdExists = false;
        for (ScriptApp app : this.apps.values()) {
            cmdExists = app.executeChatCommand(cmd, arg, channel);
            if (cmdExists)
                break;
        }
        return cmdExists;
    }

    @Override
    public void save() {
        if (!this.apps.isEmpty()) {
            for (ScriptApp app : this.apps.values())
                app.onAppStop();
        }
    }
    @Override
    public void load() {
        if (!this.apps.isEmpty()) {
            for (ScriptApp app : this.apps.values())
                app.onAppStop();
        }
        this.apps.clear();
        File appFolder = new File("data" + File.separator + "module" + File.separator + "scripts");
        if (!appFolder.exists())
            appFolder.mkdirs();
        File[] appFolders = appFolder.listFiles(new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        for (File appDir : appFolders) {
            String appName = appDir.getName();
            if (!this.apps.containsKey(appName)) {
                try {
                    ScriptApp app = new ScriptApp(appDir.getPath(), groupChat);
                    this.apps.put(app.getName(), app);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    
    // SCRIPT API
    public void onAppStart() {
        for (ScriptApp app : this.apps.values())
            app.onAppStart();
    }
    public void onAppStop() {
        for (ScriptApp app : this.apps.values())
            app.onAppStop();
    }
    
    public String onPacketReceived(String packet) {
        for (ScriptApp app : this.apps.values())
            packet = app.onPacketReceived(packet);
        return packet;
    }
    public String onPacketSent(String packet) {
        for (ScriptApp app : this.apps.values())
            packet = app.onPacketSent(packet);
        return packet;
    }
    
    public GenericProtocol onNodeReceived(GameConnection gcon, GenericProtocol node) {
        for (ScriptApp app : this.apps.values())
            node = app.onNodeReceived(gcon, node);
        return node;
    }
    public GenericProtocol onNodeSent(GameConnection gcon, GenericProtocol node) {
        for (ScriptApp app : this.apps.values())
            node = app.onNodeSent(gcon, node);
        return node;
    }
}
