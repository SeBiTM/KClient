package kclient.knuddels.tools.popup.components;

import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;

/**
 * 
 * @author SeBi
 */
public class TextField implements Component {
    private int[] foreground, background;
    private String text;
    private byte width;
    private Location location;
    
    public TextField(int width, Location location) {
	this.foreground = new int[] { 0x00, 0x00, 0x00 };
	this.background = new int[] { 0xFF, 0xFF, 0xFF };
	this.text = "";
	this.width = (byte) width;
        this.location = location;
    }

    @Override
    public ComponentType getType() {
	return ComponentType.TEXT_FIELD;
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
    public void setLocation(Location location) {
        this.location = location;
    }
    
    @Override
    public Location getLocation() {
        return this.location;
    }

    public void setText(String text) {
	this.text = text;
    }

    public byte getWidth() {
	return this.width;
    }
}
