package kclient.module.bingo.tools;

/**
 *
 * @author SeBi
 */
public enum BingoFieldState {
    ANY(0),
    NOT_SELECTED(1),
    SELECTED(2),
    DEAD(3),
    GLOWING(4);
    
    private final int state;
    private BingoFieldState(int state) {
        this.state = state;
    }
    
    public int getState() {
        return this.state;
    }
    
    public static BingoFieldState parse(byte state) {
        for (BingoFieldState st : BingoFieldState.values())
            if (st.getState() == state)
                return st;
        return null;
    }
}
