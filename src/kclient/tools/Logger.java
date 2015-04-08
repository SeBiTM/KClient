package kclient.tools;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author SeBi
 */
public class Logger {
    
    public static void debug(String message) {
        System.out.println("[DEBUG]" + message);
    }
    
    public static void error(String message) {
        System.err.println("[ERROR]" + message);
    }
    
    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }
}
