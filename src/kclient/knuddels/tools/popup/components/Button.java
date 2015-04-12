package kclient.knuddels.tools.popup.components;

import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;

/**
 * 
 * @author SeBi
 */
public class Button implements Component {
    private int[] foreground, background;
    private String text;
    private boolean styled, colored;
    private boolean close;
    private boolean action;
    private String command;
    private Location location;

    public Button(String text) {
        this(text, Location.NONE);
    }
    public Button(String text, Location location) {
	this.foreground = new int[] { 0x00, 0x00, 0x00 };
	this.background = new int[] { 0xBE, 0xBC, 0xFB };
	this.text = text;
	this.styled = false;
	this.close = true;
	this.action = false;
	this.command = null;
        this.location = location;
    }
    
    @Override
    public ComponentType getType() {
	return ComponentType.BUTTON;
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

    public boolean isStyled() {
	return this.styled;
    }

    public boolean isColored() {
	return this.colored;
    }

    public void setStyled(boolean colored) {
	this.styled = true;
	this.colored = colored;
    }

    public boolean isClose() {
	return this.close;
    }

    public void disableClose() {
	this.close = false;
    }

    public boolean isAction() {
	return this.action;
    }

    public void enableAction() {
	this.action = true;
    }

    public String getCommand() {
	return this.command;
    }

    public void setCommand(String command) {
	this.command = command;
    }
}
