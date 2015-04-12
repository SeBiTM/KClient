package kclient.knuddels.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import kclient.tools.Logger;

/**
 *
 * @author SeBi
 */
public class KClass {
    private Class clazz;
    private Object instance;
    private Map<String, Method> methods;
    private Map<String, Field> fields;
    private Map<String, Object> localStorage;
    
    public KClass(Object instance) {
        this.clazz = instance.getClass();
        this.instance = instance;
        this.methods = new HashMap<>();
        this.fields = new HashMap<>();
        this.localStorage = new HashMap<>();
    }
    public KClass(Class clazz, Object instance) {
        this.clazz = clazz;
        this.instance = instance;
        this.methods = new HashMap<>();
        this.fields = new HashMap<>();
        this.localStorage = new HashMap<>();
    }
    public KClass(KLoader loader, String className) {
        try {
            this.clazz = loader.findClass(className);
            this.instance = this.clazz.newInstance();
            this.methods = new HashMap<>();
            this.fields = new HashMap<>();
            this.localStorage = new HashMap<>();
        } catch (InstantiationException | IllegalAccessException ex) {
            Logger.get().error(ex);
        }
    }
    public KClass(String className, Object... params) {
        this(className);
        try {
            Class[] types = new Class[params.length];
            for (int i = 0; i < types.length; i++)
                types[i] = params[i].getClass();
            this.instance = this.clazz.getConstructor(types).newInstance(params);
        
            this.methods = new HashMap<>();
            this.fields = new HashMap<>();
            this.localStorage = new HashMap<>();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.get().error(ex);
        }
    }
    public KClass(Class clazz, Object... params) {
        this.clazz = clazz;
        try {
            Class[] types = new Class[params.length];
            for (int i = 0; i < types.length; i++)
                types[i] = params[i].getClass();
            this.instance = this.clazz.getConstructor(types).newInstance(params);
            
            this.methods = new HashMap<>();
            this.fields = new HashMap<>();
            this.localStorage = new HashMap<>();
        } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.get().error(ex);
        }
    }
    
    public Object getField(String field) {
        try {
            if (!this.fields.containsKey(field)) {
                Field f = this.clazz.getField(field);
                this.fields.put(field, f);
            }
            if (this.fields.containsKey(field))
                return this.fields.get(field).get(this.instance);
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            Logger.get().error(e);
        }
        return null;
    }
    
    public Object invokeMethod(String method) {
        try {
            if (this.methods == null)
                return null;
            if (!this.methods.containsKey(method)) {
                Method m = this.clazz.getMethod(method, new Class[] { });
                this.methods.put(method, m);
            }
            if (this.methods.containsKey(method)) {
                return this.methods.get(method).invoke(instance, new Object[] { });
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.get().error(ex);
        }
        return null;
    }
    public Object invokeMethod(String method, Object... params) {
        try {
            if (this.methods == null)
                return null;
            if (!this.methods.containsKey(method)) {
                Class[] types = new Class[params.length];
                for (int i = 0; i < types.length; i++) {
                    types[i] = params[i].getClass();
                    if (params[i].getClass().getName().equals("java.lang.Integer"))
                        types[i] = int.class;
                }
                Method m = this.clazz.getMethod(method, types);
                this.methods.put(method, m);
            }
            if (this.methods.containsKey(method)) {
                return this.methods.get(method).invoke(instance, params);
            }
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            Logger.get().error(ex);
        }
        return null;
    }
    
    public void add(String key, Object value) {
        if (this.localStorage.containsKey(key))
            this.localStorage.remove(key);
        this.localStorage.put(key, value);
    }
    public <T> T get(String key) {
        if (this.localStorage.containsKey(key))
            return (T)this.localStorage.get(key);
        return null;
    }
    
    public Class getSuperclass() {
        return this.clazz.getSuperclass();
    }
    public Object getInstance() {
        return this.instance;
    }
}
