package kclient.knuddels.reflection;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import kclient.knuddels.tools.ChatSystem;
import kclient.tools.Logger;

/**
 *
 * @author SeBi
 */
public class KLoader {
    private static final int STEPS = 37;
    private int currentStep = 0;

    private static final Map<ChatSystem, KLoader> loaders;
    static {
        loaders = new HashMap<>();
    }
    public static KLoader getLoader(ChatSystem cs) {
        if (!loaders.containsKey(cs)) {
            boolean copy = false;
            for (ChatSystem css : loaders.keySet())
                if (css.getVersion().equals(cs.getVersion())) {
                    loaders.put(cs, loaders.get(css));
                    copy = true;
                    break;
                }
            if (!copy)
                loaders.put(cs, new KLoader(cs));
        }
        return loaders.get(cs);
    }

    private ClassPool cp;
    private URLClassLoader loader;
    private ChatSystem system;
    private JProgressBar progress;
    private JLabel progressLbl;
    private boolean ready;
    
    public KLoader(ChatSystem cs) {
        try {
            this.system = cs;
            this.loader = new URLClassLoader(this.getApplets(cs), Thread.currentThread().getContextClassLoader());
            this.cp = ClassPool.getDefault();
            this.cp.insertClassPath(new LoaderClassPath(this.loader));
        } catch (Exception e) {
            Logger.get().error(e);
        }
    }

    public Class findClass(String name) {
        try {
            return this.loader.loadClass(name);
        } catch (ClassNotFoundException ex) {
            Logger.get().error(ex);
        }
        return null;
    }
    
    private URL[] getApplets(ChatSystem cs) {
        try {
            String version = cs.getVersion();
            URL url = new URL("jar:http://chat.knuddels.de/knuddels" + version + ".jar!/META-INF/MANIFEST.MF");
            JarURLConnection con = (JarURLConnection)url.openConnection();
            byte[] buffer = new byte[con.getContentLength()];
            int pos = 0;
            while (pos < buffer.length)
                pos += con.getInputStream().read(buffer, pos, buffer.length - pos);

            String str = new String(buffer);
            str = str.substring(str.indexOf("Class-Path:") + 12);
            str = str.replace("\r\n ", "");
            str = str.substring(0, str.indexOf("App"));

            String[] apps = str.split(" ");
            URL[] urls = new URL[apps.length + 1];
            urls[0] = new URL("jar:http://chat.knuddels.de/knuddels" + version + ".jar!/");
            for (int i = 0; i < apps.length; i++) {
                urls[i + 1] = new URL("jar:http://chat.knuddels.de/" + apps[i] + "!/");
            }

            return urls;
        } catch (IOException e) {
            Logger.get().error(e);
        }
        return null;
    }
    
    public boolean isReady() {
        return this.ready;
    }
    
    public void prepare() {
        new Thread("KClassLoaderInit") {
            @Override
            public void run() {
                KLoader.this.progress.setMaximum(KLoader.STEPS);
                while (KLoader.this.currentStep != (KLoader.STEPS + 1)) {
                    if (KLoader.this.currentStep == KLoader.STEPS)
                        break;
                    KLoader.this.progress.setValue(KLoader.this.currentStep);
                    KLoader.this.progressLbl.setText(
                        ((KLoader.this.currentStep * 100) / KLoader.STEPS) + 
                        " %"
                    );
                }
                KLoader.this.progressLbl.setText(
                    ((KLoader.this.currentStep * 100) / KLoader.STEPS) + 
                    " %"
                );
                KLoader.this.progress.setValue(KLoader.STEPS);
            }
        }.start();
        
        Logger.get().info(" - Preparing Classes...");
        this.currentStep++;
        this.prepareGroupChat();
        this.prepareModule();
        this.prepareGameHandler();
        this.prepareConnection();
        this.prepareBingoFrame();
        this.prepareGameFrame();
        this.currentStep = KLoader.STEPS;
        this.ready = true;
    }
    
    private void prepareGroupChat() {
        try {
            CtClass groupChat = this.cp.get(this.system.getManipulation().getGroupChat());
            groupChat.defrost();
        
            Logger.get().info("   - Prepare GroupChat (" + groupChat.getName() + ")");
            this.currentStep++;

            //<editor-fold defaultstate="collapsed" desc="Tunnel">
            Logger.get().info("     - Add Field tunnel");
            this.currentStep++;
            groupChat.addField(CtField.make("public kclient.knuddels.GroupChat tunnel;", groupChat));
            Logger.get().info("     - Add Method setTunnel");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
                "public void setTunnel(kclient.knuddels.GroupChat tunnel) {"
            +       "this.tunnel = $1;"
            +   "}"
            , groupChat));
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Doc/Code-Base / Paramter / Context">
            Logger.get().info("     - Change Method getParameter");
            this.currentStep++;
            groupChat.getDeclaredMethod("getParameter", new CtClass[] { cp.get("java.lang.String") }).setName("getParameterOld");
            groupChat.addMethod(CtMethod.make(
                "public String getParameter(String key) {"
            +       "String str = this.tunnel.handleParameter($1);"
            +       "if (str == null) {"
            +           "str = getParameterOld($1);"
            +       "}"
            +       "return str;"
            +   "}"
            , groupChat));

            Logger.get().info("     - Change Method getDocumentBase");
            this.currentStep++;
            groupChat.getDeclaredMethod("getDocumentBase").setName("getAppletBase");
            groupChat.addMethod(CtMethod.make(
                "public java.net.URL getDocumentBase() {"
            +        "return new java.net.URL(getParameter(\"dbase\")); "
            +   "}"
            , groupChat));

            Logger.get().info("     - Add Method getCodeBase");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
                "public java.net.URL getCodeBase() {"
            +       "return new java.net.URL(getParameter(\"cbase\")); "
            +   "}"
            , groupChat));

            Logger.get().info("     - Add Method getAppletContext");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
                "public java.applet.AppletContext getAppletContext() {"
            +       "return new kclient.knuddels.reflection.tools.KContext();"
            +   "}"
            , groupChat));
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="INPUT">
            Logger.get().info("     - Rename Method " + this.system.getManipulation().getGroupChatInput() +
                    " (Input) to processInput");
            this.currentStep++;
            CtMethod inp = groupChat.getDeclaredMethod(this.system.getManipulation().getGroupChatInput(), 
                    new CtClass[] { cp.get("java.util.StringTokenizer") });
            inp.setName("processInput");
            inp.setModifiers(1);

            Logger.get().info("     - Add Method " + this.system.getManipulation().getGroupChatInput() +
                    " (Input)");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
            "private final void " + this.system.getManipulation().getGroupChatInput() + "(java.util.StringTokenizer st) {"
            +       "String opcode = $1.nextToken();"
            +       "StringBuilder buffer = new StringBuilder(opcode);"
            +       "while ($1.hasMoreTokens()) {"
            +		"if (!opcode.equals(\"k\")) {"
            +               "buffer.append(this.tunnel.delimiter);"
            +		"}"
            +		"buffer.append($1.nextToken());"
            +       "}"

            +       "String packet = this.tunnel.handleInput(buffer.toString());"
            +       "if (packet != null) {"
            +		"processInput(new java.util.StringTokenizer(packet, this.tunnel.delimiter, opcode.equals(\"k\")));"
            +       "}"
            +   "}"
            , groupChat));
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="OUTPUT">
            Logger.get().info("     - Rename Method " + this.system.getManipulation().getGroupChatOutput() +
                    " (Output) to processOutput");
            this.currentStep++;
            CtMethod out = groupChat.getDeclaredMethod(this.system.getManipulation().getGroupChatOutput(), 
                    new CtClass[] { cp.get("java.lang.String"), 
                        cp.get("boolean"), 
                        cp.get("int") 
                    });
            out.setName("processOutput");
            out.setModifiers(1);

            Logger.get().info("     - Add Method " + this.system.getManipulation().getGroupChatOutput() +
                    " (Output)");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
                "public synchronized boolean " + this.system.getManipulation().getGroupChatOutput() + "(java.lang.String str, boolean b, int i) {"
            +       "$1 = this.tunnel.handleOutput($1);"
            +       "if ($1 != null) {"
            +           "return processOutput($1, $2, $3);"
            +       "}"
            +       "return true;"
            +   "}"
            , groupChat));
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="MODULE">
            Logger.get().info("     - Rename Method " + this.system.getManipulation().getGroupChatMdlInput() +
                    " (ModuleInput) to processModule");
            CtMethod mdl = groupChat.getDeclaredMethod(this.system.getManipulation().getGroupChatMdlInput(), 
                    new CtClass[] { cp.get("java.lang.String"), cp.get("int") });
            mdl.setName("processModule");
            mdl.setModifiers(1);

            Logger.get().info("     - Add Method " + this.system.getManipulation().getGroupChatMdlInput() +
                    " (ModuleInput)");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
                "final void " + this.system.getManipulation().getGroupChatMdlInput() + "(String packet, int pos) {"
            +       "$1 = this.tunnel.handleInput($1);"
            +       "if ($1 != null) {"
            +           "processModule($1, $2);"
            +       "}"
            +   "}"
            , groupChat));
            //</editor-fold>

            //<editor-fold defaultstate="collapsed" desc="Receive / Send">
            Logger.get().info("     - Add Method receive");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
            "public void receive(String packet) {"
            +       "if ($1.startsWith(\":\")) {"
            +           "processModule($1, 2);"
            +       "} else {"
            +          "processInput(new java.util.StringTokenizer($1, this.tunnel.delimiter, $1.startsWith(\"k\")));"
            +       "}"
            +   "}"
            , groupChat));

            Logger.get().info("     - Add Method send");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
            "public void send(String packet) {"
            +       "processOutput($1, false, 0);"
            +   "}"
            , groupChat));
            //</editor-fold>

            /*groupChat.addMethod(CtMethod.make(
            "public void login(String nick, String pass, String chan) {"
            +   this.system.getManipulation().getGroupChatLogin() + "($1, $2, $3, false);"
            + "}", groupChat));
            */
            Logger.get().info("     - Add Method getChannels");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
            "public java.util.Enumeration getChannels() {"
            +       "return " + this.system.getManipulation().getGroupChatChannels() + "();"
            +   "}"
            , groupChat));

            Logger.get().info("     - Add Method getModuleParent");
            this.currentStep++;
            groupChat.addMethod(CtMethod.make(
            "public " + this.system.getManipulation().getModuleParent() + " getModuleParent() {"
            +       "return " + this.system.getManipulation().getGroupChatModule() + "();"
            +   "}"
            , groupChat));

            groupChat.getDeclaredMethod("a", new CtClass[] {
                cp.get("java.lang.String"),
                cp.get("java.lang.Throwable"),
                cp.get("java.lang.String")
            }).setBody("{ }");

            this.cp.toClass(groupChat, this.loader);
        } catch (CannotCompileException | NotFoundException e) {
            Logger.get().error(e);
        }
    }
    private void prepareModule() {
        try {
            CtClass module = this.cp.get(this.system.getManipulation().getModuleParent());
            module.defrost();
            
            Logger.get().info("   - Prepare Module (" + module.getName() + ")");
            this.currentStep++;

            module.addField(CtField.make("private String tree;", module));
            this.currentStep++;
            Logger.get().info("     - Add Field tree");

            Logger.get().info("     - Change Method reset (" + this.system.getManipulation().getModuleReset() + ")");
            this.currentStep++;
            module.getDeclaredMethod(this.system.getManipulation().getModuleReset(), new CtClass[] { cp.get("java.lang.String") }).insertBefore(
             "{"
            +   "if ($1 != null) {"
            +       "tree = $1;"
            +   "}"
            +"}");

            Logger.get().info("     - Add Method getTree");
            this.currentStep++;
            module.addMethod(CtMethod.make(
                "public String getTree() {"
            +       "return tree;"
            +   "}"
            , module));

            this.cp.toClass(module, loader);
        } catch (NotFoundException | CannotCompileException ex) {
            Logger.get().error(ex);
        }
    }
    private void prepareGameHandler() {
        try {
            CtClass handler = this.cp.get(this.system.getManipulation().getGameHandlerClass());
            handler.defrost();
            Logger.get().info("   - Prepare GameHandler (" + handler.getName() + ")");
            this.currentStep++;

            Logger.get().info("     - Change Field Modifier");
            this.currentStep++;
            handler.getDeclaredField(this.system.getManipulation().getGameHandlerField()).setModifiers(1);

            this.cp.toClass(handler, loader);
        } catch (CannotCompileException | NotFoundException e) {
            Logger.get().error(e);
        }
    }
    private void prepareConnection() {
        try {
            CtClass connection = this.cp.get(this.system.getManipulation().getConnectionClass());
            connection.defrost();
            Logger.get().info("   - Prepare Connection (" + connection.getName() + ")");
            this.currentStep++;

            Logger.get().info("     - Add Field tunnel");
            this.currentStep++;
            connection.addField(CtField.make(
                "public kclient.knuddels.GroupChat tunnel;"
            , connection));

            Logger.get().info("     - Add Method getModuleParent");
            this.currentStep++;
            connection.addMethod(CtMethod.make(
                "public " + this.system.getManipulation().getModuleParent() + " getModuleParent() {"
            +        "return " + this.system.getManipulation().getConnectionModule() + ";"
            +   "}"
            , connection));

            Logger.get().info("     - Add Method send");
            this.currentStep++;
            connection.addMethod(CtMethod.make(
                "public void send(byte[] buffer) {"
            +        this.system.getManipulation().getConnectionOutput() + "(" + this.system.getManipulation().getConnectionModule() + "." + this.system.getManipulation().getModuleRead() + "($1));"
            +   "}"
            , connection));

            Logger.get().info("     - Add Method receive");
            this.currentStep++;
            connection.addMethod(CtMethod.make(
                "public void receive(byte[] buffer) {"
            +       this.system.getManipulation().getConnectionField() + "." + this.system.getManipulation().getConnectionFieldInput() + "(" + this.system.getManipulation().getConnectionModule() + "." + this.system.getManipulation().getModuleRead() + "($1));"
            +   "}"
            , connection));

            Logger.get().info("     - Add Method getTunnel");
            this.currentStep++;
            connection.addMethod(CtMethod.make(
                "private kclient.knuddels.GroupChat getTunnel() {"
            +       "if (this.tunnel == null) {"
            +           "this.tunnel = ((" + this.system.getManipulation().getGroupChat() + ")((" + this.system.getManipulation().getGameHandlerClass() + ")this." + this.system.getManipulation().getConnectionField() + ")." + this.system.getManipulation().getGameHandlerField() + ").tunnel;"
            +       "}"
            +       "return this.tunnel;"
            +   "}"
            , connection));

            Logger.get().info("     - Change Output Method (" + this.system.getManipulation().getConnectionOutput() + ")");
            this.currentStep++;
            connection.getDeclaredMethod(this.system.getManipulation().getConnectionOutput(), new CtClass[] {
                cp.get(this.system.getManipulation().getModuleClass())
            }).insertBefore(
                "{"
            +       "if (this.getTunnel() != null) {"
            +           "$1 = " + this.system.getManipulation().getConnectionModule() + "." + this.system.getManipulation().getModuleRead() + "(" + 
                                  "this.getTunnel().handleExtendOutput(" 
            +               "new kclient.knuddels.reflection.KClass(this), " 
            +               this.system.getManipulation().getConnectionModule() + "." + this.system.getManipulation().getModuleWrite() + "($1)"
            +           "));"
            +       "}"
            +   "}");

            Logger.get().info("     - Change Input Method (run)");
            this.currentStep++;
            connection.getDeclaredMethod("run")
                    .setBody(
                "{"
            +       "if (" + this.system.getManipulation().getConnectionConnect() + "()) {"
            +           "while (" + this.system.getManipulation().getConnectionConnected() + ") {"
            +               "try {"
            +                   "byte[] buffer = " + this.system.getManipulation().getConnectionRead() + "(" + this.system.getManipulation().getConnectionStream() + ");"
            +                   this.system.getManipulation().getModuleClass() + " mdl = " + this.system.getManipulation().getConnectionModule() + "." + this.system.getManipulation().getModuleRead()
            +                       "(this.getTunnel().handleExtendInput("
            +                           "new kclient.knuddels.reflection.KClass(this),"
            +                           "buffer"
            +                       "));"
            +                   "if (!this." + this.system.getManipulation().getConnectionModuleCheck() + "(mdl)) {"
            +                       this.system.getManipulation().getConnectionField() + "." + this.system.getManipulation().getConnectionFieldInput() + "(mdl);"
            +                   "}"
            +               "} catch (Exception e) {"
            +                   "e.printStackTrace();"
            +                   this.system.getManipulation().getConnectionClose() + "();"
            +               "}"
            +           "}"
            +       "}"
            +   "}");
            this.cp.toClass(connection, loader);
        } catch (CannotCompileException | NotFoundException e) {
            Logger.get().error(e);
        }
    }
    private void prepareBingoFrame() {
        try {
            CtClass bingoFrame = this.cp.get(this.system.getManipulation().getBingoFrameClass());
            Logger.get().info("   - Prepare BingoFrame (" + bingoFrame.getName() + ")");
            this.currentStep++;
            //<editor-fold defaultstate="collapsed" desc="TUNNEL">
            
            Logger.get().info("     - Add Field tunnel");
            bingoFrame.addField(CtField.make("private kclient.knuddels.GroupChat tunnel;", bingoFrame));
            
            Logger.get().info("     - Add Method check");
            this.currentStep++;
            bingoFrame.addMethod(CtMethod.make(
                "private boolean check() {"
            +       "if (this.tunnel == null) {"
            +           "this.tunnel = ((" + this.system.getManipulation().getGroupChat() + ")this." + 
                        this.system.getManipulation().getBingoFrameGroupChat() + ").tunnel;"
            +       "}"
            +       "return this.tunnel != null;"
            +   "}"
            , bingoFrame));
            //</editor-fold>
         
            Logger.get().info("     - Change Constructor");
            this.currentStep++;
            CtConstructor c = bingoFrame.getDeclaredConstructors()[0];
            c.insertAfter(
                "{"
            +       "if (!check()) {"
            +           "System.err.println(\"Tunnel not set ;(\");"
            +       "} else {"
            +           "kclient.knuddels.reflection.KClass kc = new kclient.knuddels.reflection.KClass(this);"
            +           "kc.add(\"sheetId\", new Long($3));"
            +           "this.tunnel.handleFrame(0, kc);"
            +       "}"
            +   "}"
            );
            
            bingoFrame.addMethod(CtMethod.make(
                "public void setVisible(boolean v) {"
            +       "if (check()) {"
            +           "if (this.tunnel.getBingoVisible() && $1 == true) {"
            +               "super.setVisible($1); "
            +           "} else if (!$1) {"
            +               "super.setVisible($1);"
            +           "}"
            +       "} else {"
            +           "super.setVisible($1);"
            +       "}"
            +   "}", bingoFrame));
            
            this.cp.toClass(bingoFrame, this.loader);
        } catch (Exception e) {
            Logger.get().error(e);
        }
    }
    private void prepareGameFrame() {
        try {
            CtClass gameFrame = this.cp.get(this.system.getManipulation().getGameFrameClass());
            Logger.get().info("   - Prepare GameFrame (" + gameFrame.getName() + ")");
            this.currentStep++;
            //<editor-fold defaultstate="collapsed" desc="TUNNEL">
            
            Logger.get().info("     - Add Field tunnel");
            gameFrame.addField(CtField.make("private kclient.knuddels.GroupChat tunnel;", gameFrame));
            
            Logger.get().info("     - Add Method check");
            this.currentStep++;
            gameFrame.addMethod(CtMethod.make(
                "private boolean check() {"
            +       "if (this.tunnel == null) {"
            +           "this.tunnel = this." + 
                        this.system.getManipulation().getGameFrameHelper() + "().tunnel;"
            +       "}"
            +       "return this.tunnel != null;"
            +   "}"
            , gameFrame));
            //</editor-fold>
         
            Logger.get().info("     - Change Init Method (" + this.system.getManipulation().getGameFrameVoid() + ")");
            this.currentStep++;
            gameFrame.getDeclaredMethod(this.system.getManipulation().getGameFrameVoid(), new CtClass[] { this.cp.get("java.awt.Dimension") }).insertAfter(
                "{"
            +       "if (!check()) {"
            +           "System.err.println(\"Tunnel not set ;(\");"
            +       "} else {"
            +           "kclient.knuddels.reflection.KClass kc = new kclient.knuddels.reflection.KClass(this);"
            +           "this.tunnel.handleFrame(1, kc);"
            +       "}"
            +   "}"
            );

            this.cp.toClass(gameFrame, this.loader);
        } catch (Exception e) {
            Logger.get().error(e);
        }
    }
    
    public void setProgress(JProgressBar p, JLabel lbl) {
        this.progress = p;
        this.progressLbl = lbl;
    }
}
