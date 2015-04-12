package kclient.knuddels.tools.popup.components;

import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;

/**
 * 
 * @author SeBi
 */
public class TextArea implements Component {
    private int[] foreground, background;
    private String text;
    private boolean editable;
    private byte scrollbars;
    private byte rows, columns;
    private Location location;
    
    public TextArea(int rows, int columns, Location location) {
	this.foreground = new int[] { 0x00, 0x00, 0x00 };
        this.background = new int[] { 0xFF, 0xFF, 0xFF };
	this.text = "";
	this.editable = true;
	this.scrollbars = 1;
	this.rows = (byte) rows;
	this.columns = (byte) columns;
        this.location = location;
    }

    @Override
    public ComponentType getType() {
	return ComponentType.TEXT_AREA;
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

    public boolean isEditable() {
	return this.editable;
    }

    public void disable() {
	this.editable = false;
    }

    public byte getScrollbars() {
	return this.scrollbars;
    }

    public void setScrollbars(int scrollbars) {
	this.scrollbars = (byte) scrollbars;
    }

    public byte getRows() {
	return this.rows;
    }

    public byte getColumns() {
	return this.columns;
    }
}
