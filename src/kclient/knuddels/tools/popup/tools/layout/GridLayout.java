package kclient.knuddels.tools.popup.tools.layout;

/**
 *
 * @author SeBi
 */
public class GridLayout implements Layout {
    private int rows, cols, hgap, vgap;
    
    public GridLayout(int rows, int cols) {
        this(rows, cols, 0, 0);
    }
    public GridLayout(int rows, int cols, int hgap, int vgap) {
        this.rows = rows;
        this.cols = cols;
        this.hgap = hgap;
        this.vgap = vgap;
    }
    
    public int getRows() {
        return this.rows;
    }
    public int getCols() {
        return this.cols;
    }
    public int getHGap() {
        return this.hgap;
    }
    public int getVGap() {
        return this.vgap;
    }
    
    @Override
    public LayoutType getType() {
        return LayoutType.GRID_LAYOUT;
    }
    
}
