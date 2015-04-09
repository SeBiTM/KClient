package kclient.knuddels.tools.toolbar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.tools.Logger;

/**
 *
 * @author SeBi
 */
public class Toolbar {
    private final Map<String, Button> buttons;
    private final GroupChat groupChat;
    
    public Toolbar(GroupChat groupChat) {
        this.groupChat = groupChat;
        this.buttons = new HashMap<>();
    }
    
    public void addButton(Button button) {
        if (this.buttons.containsKey(button.getText()))
            this.buttons.remove(button.getText());
        this.buttons.put(button.getText(), button);
    }
    public Button getButton(String text) {
        if (this.buttons.containsKey(text))
            return this.buttons.get(text);
        return null;
    }
    
    public void clear() {
        this.buttons.clear();
    }
    
    public void referesh(String channel, boolean show) {
        GenericProtocol SHOW_BUTTONS = this.groupChat.getBaseNode().copyRef("SHOW_BUTTONS");
        SHOW_BUTTONS.add("CHANNEL_NAME", channel);
        SHOW_BUTTONS.add("ANALOG_BUTTON", new ArrayList());
        if (!show) {
            SHOW_BUTTONS.add("BUTTON", new ArrayList());
        } else {
            List<GenericProtocol> list = new ArrayList();
            for (Button btn : this.buttons.values()) {
                GenericProtocol BUTTON = SHOW_BUTTONS.copyRef("BUTTON");
                BUTTON.add("TEXT", btn.getText());
                BUTTON.add("IMAGE", btn.getImage() == null ? " " : btn.getImage());
                BUTTON.add("CHAT_FUNCTION", btn.getAction() == null ? " " : btn.getAction());
                BUTTON.add("BUTTON_ALIGN", (byte)(btn.isLeft() ? 1 : 0));
                list.add(BUTTON);
            }
            SHOW_BUTTONS.add("BUTTON", list);
        }
        try {
          this.groupChat.receive(":\u0000" + this.groupChat.getBaseNode().toString(SHOW_BUTTONS));
        } catch (Exception ex) {
            Logger.get().error(ex.toString());
        }
    }
}
