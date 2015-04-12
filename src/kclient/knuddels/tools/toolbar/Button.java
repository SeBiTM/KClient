package kclient.knuddels.tools.toolbar;

/**
 *
 * @author SeBi
 */
public class Button {
    private String text, image, action;
    private boolean left;
    
    public Button(String text) {
        this(text, null);
    }
    public Button(String text, String image) {
        this(text, image, true);
    }
    public Button(String text, String action, boolean left) {
        this(text, null, action, left);
    }
    public Button(String text, String image, String action) {
        this(text, image, action, true);
    }
    public Button(String text, String image, String action, boolean left) {
        this.text = text;
        this.image = image;
        this.action = action;
        this.left = left;
    }

    public String getText() {
        return this.text;
    }
    public String getImage() {
        return this.image;
    }
    public String getAction() {
        return this.action;
    }
    public boolean isLeft() {
        return this.left;
    }
}
