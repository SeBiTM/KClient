package kclient.module.bingo.tools;

/**
 *
 * @author SeBi
 */
public enum BingoSheetState {
    ACTIVE(0),
    INACTIVE(1),
    BINGO(2),
    NO_BINGO(3),
    JACKPOT(4),
    GAME_END(5);
    
    private final int state;
    private BingoSheetState(int state) {
        this.state = state;
    }
    
    public int getState() {
        return this.state;
    }
    
    public static BingoSheetState parse(int state) {
        for (BingoSheetState st : BingoSheetState.values())
            if (st.getState() == state)
                return st;
        return null;
    }
}
