package kclient.knuddels.tools.popup.tools;

/**
 * 
 * @author SeBi
 */
public enum ComponentType {
    BUTTON('b'), TEXT_FIELD('f'), LABEL('l'), TEXT_AREA('t'), CHECKBOX('x'), PANEL('p'), TEXT_PANEL('c'), CHOICE('o');

    private final int type;
    private ComponentType(int type) {
        this.type = type;
    }
    public int getValue() {
        return type;
    }
}
