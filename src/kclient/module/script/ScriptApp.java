package kclient.module.script;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.tools.Parameter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Function;
import org.mozilla.javascript.NativeObject;
import org.mozilla.javascript.ScriptableObject;

/**
 *
 * @author SeBi
 */
public class ScriptApp {
    private String path;
    private Context context;
    private ScriptableObject scope;
    private NativeObject appObject;
    private Map<String, Function> appHooks;
    private Map<String, Function> chatCommands;
    private final GroupChat groupChat;
    
    public ScriptApp(String path, GroupChat groupChat) {
        this.path = path;
        this.groupChat = groupChat;
        this.init();
    }
    
    private String loadFile(String name) {
        try {
            byte[] arr = Files.readAllBytes(Paths.get(this.path, name));
            return new String(arr, "UTF-8");
        } catch (IOException ex) {
            Logger.getLogger(ScriptApp.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public void init() {
        this.appHooks = new HashMap<>();
        this.chatCommands = new HashMap<>();
        try {
            String mainSource = loadFile("main.js");

            this.context = Context.enter();
            this.context.setOptimizationLevel(-1);
            this.context.setLanguageVersion(180);
            
            this.scope = this.context.initStandardObjects();
            ScriptableObject.putProperty(this.scope, "GroupChat", Context.javaToJS(this.groupChat, this.scope));
            
            eval(mainSource);
            
            Object functionObject = this.scope.get("App");
            if ((functionObject instanceof NativeObject)) {
                this.appObject = (NativeObject)functionObject;
                List<String> strHooks = Arrays.asList(new String[] { 
                    "onAppStart", "onAppStop",
                    "onPacketReceived", "onPacketSent",
                    "onNodeReceived", "onNodeSent" 
                });
                for (Object hook : this.appObject.getIds()) {
                    if (strHooks.contains(hook.toString())) {
                        Function hookFunc = (Function) this.appObject.get(hook.toString());
                        if (!this.appHooks.containsKey(hook.toString()))
                            this.appHooks.put(hook.toString(), hookFunc);
                    } else if (hook.toString().equals("chatCommands")) {
                        NativeObject commands = (NativeObject)this.appObject.get("chatCommands");
                        for (Object o : commands.getAllIds()) {
                            String cmd = o.toString();
                            if (!this.chatCommands.containsKey(cmd)) {
                                this.chatCommands.put(cmd.toLowerCase(), (Function)commands.get(cmd));
                            }
                        }
                    }
                }
                
                
                System.out.println("AppHooks " + Arrays.toString(appHooks.keySet().toArray()));
                System.out.println("AppCommands " + Arrays.toString(chatCommands.keySet().toArray()));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void eval(String source) {
        try {
            this.context.evaluateString(this.scope, source, "", 1, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void include(String file) {
        eval(loadFile(file));
    }
    
    private <T> T callHook(String hook, Object... params) {
        try {
            if (this.appHooks.containsKey(hook.toLowerCase())) {
                Object result = this.appHooks.get(hook.toLowerCase()).call(this.context, this.scope, this.scope, params);
                if (result.toString().equals("false"))
                    return null;
                return (T) result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void onAppStart() {
        callHook("onAppStart");
    }
    public void onAppStop() {
        callHook("onAppStop");
    }
    
    public String onPacketReceived(String packet) {
        if (!this.appHooks.containsKey("onPacketRecveived"))
            return packet;
        return (String) callHook("onPacketReceived", packet);
    }
    public String onPacketSent(String packet) {
        if (!this.appHooks.containsKey("onPacketSent"))
            return packet;
        return (String) callHook("onPacketSent", packet);
    }
    
    public GenericProtocol onNodeReceived(GameConnection gameConnection, GenericProtocol node) {
        if (!this.appHooks.containsKey("onNodeReceived"))
            return node;
        return (GenericProtocol) callHook("onNodeReceived", gameConnection, node);
    }
    public GenericProtocol onNodeSent(GameConnection gameConnection, GenericProtocol node) {
        if (!this.appHooks.containsKey("onNodeSent"))
            return node;
        return (GenericProtocol) callHook("onNodeSent", gameConnection, node);
    }
    
    public boolean executeChatCommand(String cmd, String arg, String channel) {
        if (this.chatCommands.containsKey(cmd.toLowerCase())) {
            return (boolean) this.chatCommands.get(cmd.toLowerCase()).call(this.context, this.scope, this.scope, new Object[] { cmd, arg, channel });
        }
        return false;
    }
}
