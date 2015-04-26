package kclient.knuddels.tools.popup.components;

import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;

/**
 *
 * @author SeBi
 */
public class TextPanel implements Component {
    private int[] foreground, background;
    private int position, width, height;
    private String text, backgroundImage;
    private Location location;
    private String id, updateId;

    public TextPanel(String text, int width, int height, Location loc) {
        this(text, width, height, new int[] {0,0,0}, new int[] {0xBE, 0xBC, 0xFB}, "pics/cloudsblue.gif", 17, loc);
    }
    public TextPanel(String text, int width, int height, int[] foreground, int[] background, String backgroundImage, int pos, Location loc) {
        this.foreground = foreground;
	this.background = background;
	this.text = text;
        this.location = loc;
        this.backgroundImage = backgroundImage;
        this.position = pos;
        this.height = height;
        this.width = width;
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.TEXT_PANEL;
    }
    @Override
    public int[] getForeground() {
        return this.foreground;
    }
    @Override
    public void setForeground(int[] foreground) {
        this.foreground = foreground;
    }
    @Override
    public int[] getBackground() {
	return this.background;
    }
    @Override
    public void setBackground(int[] background) {
	this.background = background;
    }
    @Override
    public String getText() {
        return this.text;
    }
    @Override
    public void setLocation(Location loc) {
        this.location = loc;
    }
    @Override
    public Location getLocation() {
        return this.location;
    }
    
    public int getPosition() {
        return this.position;
    }
    public String getBackgroundImage() {
        return this.backgroundImage;
    }
    
    public int getWidth() {
        return this.width;
    }
    public int getHeight() {
        return this.height;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    public String getId() {
        return this.id;
    }
    
    public void setUpdateId(String id) {
        this.updateId = id;
    }
    public String getUpdateId() {
        return this.updateId;
    }
}
