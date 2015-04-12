package kclient.knuddels.tools.popup.tools.layout;

/**
 *
 * @author SeBi
 */
public enum LayoutType {
    BORDER_LAYOUT('B'), FLOW_LAYOUT('F'), GRID_LAYOUT('G');
    
    private final int lay;
    private LayoutType(int lay) {
        this.lay = lay;
    }
    public int getValue() {
        return this.lay;
    }
}
