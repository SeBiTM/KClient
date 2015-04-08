package kclient.tools;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author SeBi
 */
public class Parameter {
    private static final Parameter default_params;
    static {
        default_params = new Parameter("params");
    }
    
    public static Parameter getDefault() {
        return default_params;
    }
    
    private final Map<String, String> values;
    
    public Parameter(String file) {
        this.values = new HashMap<>();
        file = "data" + File.separator + file + ".properties";
            
        FileReader reader = null;
        Properties props = new Properties();
        try {
            reader = new FileReader(file);
            props.load(reader);
        } catch (IOException e) {
            Logger.error(e.toString());
        } finally {
            try {
                if (reader != null)
                    reader.close();
            } catch (IOException e) {
                Logger.error(e.toString());
            }
        }
        
        for (String key : props.stringPropertyNames())
            this.values.put(key.toLowerCase(), props.getProperty(key));
        props.clear();
    }
    
    public Collection<Map.Entry<String, String>> getEntrySet() {
        return this.values.entrySet();
    }
    
    public Collection<String> getAll() {
        return this.values.values();
    }
    
    public boolean containsKey(String key) {
        return this.values.containsKey(key.toLowerCase());
    }
    
    public String get(String key) {
        if (this.values.containsKey(key.toLowerCase()))
            return this.values.get(key.toLowerCase());
        return null;
    }
    
    public Color getColor(String key) {
        String strColor = get(key);
        if (strColor == null || strColor.isEmpty())
            return Color.black;
        return Color.decode("#" + strColor);
    }
    
    public void close() {
        this.values.clear();
    }
}
