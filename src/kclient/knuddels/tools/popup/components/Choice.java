package kclient.knuddels.tools.popup.components;

import kclient.knuddels.tools.popup.tools.Component;
import kclient.knuddels.tools.popup.tools.ComponentType;
import kclient.knuddels.tools.popup.tools.Location;

/**
 *
 * @author SeBi
 */
public class Choice implements Component {
    private boolean disabled;
    private String selected;
    private int selectedIndex;
    private String[] items;
    private int[] foreground;
    private int[] background;
    private int fontsize;
    private Location location;

    public Choice(String[] items, Location loc) {
        this(items, null, loc);
    }
    public Choice(String[] items, String selected, Location loc) {
        this(items, selected, new int[] { 0x00, 0x00, 0x00 }, new int[] { 0xFF, 0xFF, 0xFF }, -1, false, loc);
    }
    public Choice(String[] items, String selected, int[] foreground, int[] background, int fontsize, boolean disabled, Location loc) {
        this.items = items;
        this.selected = selected;
        this.foreground = foreground;
        this.background = background;
        this.fontsize = fontsize;
        this.disabled = disabled;
        this.location = loc;
    }

    @Override
    public ComponentType getType() {
        return ComponentType.CHOICE;
    }
    @Override
    public int[] getForeground() {
        return foreground;
    }
    @Override
    public void setForeground(int[] foreground) {
        this.foreground = foreground;
    }
    @Override
    public int[] getBackground() {
        return background;
    }
    @Override
    public void setBackground(int[] background) {
        this.background = background;
    }
    @Override
    public String getText() {
        return null;
    }
    @Override
    public Location getLocation() {
        return this.location;
    }
    @Override
    public void setLocation(Location loc) {
        this.location = loc;
    }

    public boolean isDisabled() {
        return disabled;
    }

    public void toggleDisabled() {
        disabled = !disabled;
    }

    public boolean useIndex() {
        return selected == null;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int value) {
        selectedIndex = value;
    }

    public String getSelected() {
        return selected;
    }

    public void setSelected(String value) {
        selected = value;
    }

    public String[] getItems() {
        return items;
    }
    
    public void setItems(String[] value) {
        items = value;
    }

    public int getFontsize() {
        return fontsize;
    }

    public void setFontsize(int value) {
        fontsize = value;
    }
}  
