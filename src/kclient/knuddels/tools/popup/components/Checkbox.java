package kclient.knuddels.tools.popup.components;

import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;

/**
 * 
 * @author SeBi
 */
public class Checkbox implements Component {
    private int[] foreground, background;
    private String text;
    private boolean disabled;
    private boolean checked;
    private byte group;
    private Location location;
    
    public Checkbox(String text, boolean checked, Location location) {
	this.foreground = new int[] { 0x00, 0x00, 0x00 };
	this.background = new int[] { 0xBE, 0xBC, 0xFB };
	this.text = text;
	this.disabled = false;
	this.checked = checked;
	this.group = 0;
        this.location = location;
    }
   
    @Override
    public ComponentType getType() {
	return ComponentType.CHECKBOX;
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

    public boolean isDisabled() {
	return this.disabled;
    }

    public void disable() {
	this.disabled = true;
    }

    public boolean isChecked() {
	return this.checked;
    }

    public void check() {
	this.checked = true;
    }

    public byte getGroup() {
	return this.group;
    }

    public void setGroup(int group) {
	this.group = (byte) group;
    }
}
