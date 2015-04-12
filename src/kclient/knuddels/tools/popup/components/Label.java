package kclient.knuddels.tools.popup.components;

import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;

/**
 * 
 * @author SeBi
 */
public class Label implements Component {
    private int[] foreground, background;
    private String text;
    private Location location;
    private int size, style;

    public Label(String text, Location loc) {
        this(text, loc, 12);
    }
    public Label(String text, Location loc, int size) {
        this(text, loc, size, 'p', new int[] { 0xBE, 0xBC, 0xFB });
    }
    public Label(String text, Location loc, int size, char style) {
        this(text, loc, size, style, new int[] { 0xBE, 0xBC, 0xFB });
    }
    public Label(String text, Location loc, int size, char style, int[] background) {
        this.foreground = new int[] { 0x00, 0x00, 0x00 };
	this.background = background;
	this.text = text;
        this.location = loc;
        this.size = size;
        this.style = style;
    }
    
    @Override
    public ComponentType getType() {
        return ComponentType.LABEL;
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
    
    public int getSize() {
        return this.size;
    }
    public int getStyle() {
        return this.style;
    }
}
