package kclient.module.bingo.tools;

/**
 *
 * @author SeBi
 */
public class BingoField {
    private final int index;
    private final short number;
    private BingoFieldState state;
    
    public BingoField(int index, short number, byte state) {
        this.index = index;
        this.number = number;
        this.state = BingoFieldState.parse(state);
    }
    
    public int getIndex() {
        return this.index;
    }
    public short getNumber() {
        return this.number;
    }
    public BingoFieldState getState() {
        return this.state;
    }
    
    public void setState(byte state) {
        this.state = BingoFieldState.parse(state);
    }
    
    @Override
    public String toString() {
        return "Field { Index: "+this.index+", Number: " + this.number + ", State: " + this.state + "}";
    }
}
