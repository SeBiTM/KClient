package kclient.knuddels.tools.popup.tools;

/**
 *
 * @author SeBi
 */
public enum Location {
    SOUTH('S'), EAST('E'), WEST('W'), CENTER('C'), NORTH('N'), NONE('N');
    
    private final int loc;
    private Location(int loc) {
        this.loc = loc;
    }
    public int getValue() {
        return this.loc;
    }
}
