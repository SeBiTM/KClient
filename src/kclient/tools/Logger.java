package kclient.tools;

/**
 *
 * @author SeBi
 */
public class Logger {
    private static final Logger instance;
    static {
        instance = new Logger();
    }
    
    public static Logger get() {
        synchronized (instance) {
            return instance;
        }
    }
    
    private Level level = Level.ALL;
    public void setLevel(Level level) {
        this.level = level;
    }
    
    public void debug(String message) {
        if (this.level == Level.DEBUG || this.level == Level.ALL)
            System.out.println("[DEBUG] " + message);
    }
    
    public void error(Object message) {
        if (this.level == Level.ERROR || this.level == Level.ALL) {
            if (message instanceof Exception) {
                System.out.println("[ERROR] ");
                ((Exception)message).printStackTrace();
            } else {
                System.err.println("[ERROR] " + message);
            }
        }
    }
    
    public void info(String message) {
        if (this.level == Level.INFO || this.level == Level.ALL)
            System.out.println("[INFO] " + message);
    }
    
    public enum Level {
        INFO,
        DEBUG,
        ERROR,
        ALL,
        NONE
    }
}
