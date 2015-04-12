package kclient.knuddels.tools.popup.components;

import java.util.ArrayList;
import java.util.List;
import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;
import kclient.knuddels.tools.popup.tools.layout.BorderLayout;
import kclient.knuddels.tools.popup.tools.layout.Layout;

/**
 * 
 * @author SeBi
 */
public class Panel implements Component {
    private final List<Component> components;
    private Location location;
    private Layout layout;
    private String backgroundImage;
    private int height, width;
    
    public Panel(Location location) {
        this(new BorderLayout(), location);
    }
    public Panel(Layout layout, Location location) {
        this(layout, location, null, 0, 0);
    }
    public Panel(Layout layout, Location location, String backgroundImage, int height, int width) {
        this.components = new ArrayList<>();
        this.layout = layout;
        this.location = location;
        this.backgroundImage = backgroundImage;
        this.height = height;
        this.width = width;
    }

    public List<Component> getComponents() {
        return components;
    }
    public void addComponent(Component component) {
        this.components.add(component);
    }

    @Override
    public ComponentType getType() {
        return ComponentType.PANEL;
    }

    @Override
    public int[] getForeground() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setForeground(int[] foreground) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int[] getBackground() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setBackground(int[] background) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getText() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void setLocation(Location location) {
        this.location = location;
    }
    
    @Override
    public Location getLocation() {
        return this.location;
    }
    
    public void setLayout(Layout lay) {
        this.layout = lay;
    }
    public Layout getLayout() {
        return this.layout;
    }
    public String getBackgroundImage() {
        return this.backgroundImage;
    }
    public int getHeight() {
        return this.height;
    }
    public int getWidth() {
        return this.width;
    }
}
