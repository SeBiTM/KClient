package kclient.knuddels.tools.popup.tools;

/**
 * 
 * @author Flav
 * @since 1.0
 */
public interface Component {
    public ComponentType getType();
    public int[] getForeground();
    public void setForeground(int[] foreground);
    public int[] getBackground();
    public void setBackground(int[] background);
    public String getText();
    public void setLocation(Location loc);
    public Location getLocation();
}
