package kclient.knuddels.tools.toolbar;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.tools.Logger;
import kclient.tools.Parameter;

/**
 *
 * @author SeBi
 */
public class Toolbar {
    private final Map<String, Button> buttons;
    private final GroupChat groupChat;
    private final Parameter config;
    
    public Toolbar(GroupChat groupChat) {
        this.groupChat = groupChat;
        this.buttons = new HashMap<>();
        this.config = new Parameter("module" + File.separator + "toolbar");
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
    
    public void refresh(String channel, boolean show) {
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
            if (this.config.get("STYLE").equalsIgnoreCase("custom"))
                this.setStyle(channel);
        } catch (Exception ex) {
            Logger.get().error(ex);
        }
    }
    
    private void setStyle(String channel) {
        GenericProtocol settings = this.groupChat.getBaseNode().copyRef("BUTTON_BAR_SETTINGS");
        settings.add("CHANNEL_NAME", channel);
        GenericProtocol barTrend = settings.copyRef("BAR_TREND");
        barTrend.add("SPECULAR_SHADING", specularShading("BAR"));
        settings.add("BAR_TREND", barTrend);
        
        GenericProtocol buttonTrend = settings.copyRef("BUTTON_TREND");
        buttonTrend.add("SPECULAR_SHADING", specularShading("BUTTON"));
        settings.add("BUTTON_TREND", buttonTrend);
        
        GenericProtocol analogTrend = settings.copyRef("ANALOG_TREND");
        analogTrend.add("SPECULAR_SHADING", specularShading("ANALOG"));
        settings.add("ANALOG_TREND", analogTrend);
        
        try {
          this.groupChat.receive(":\u0000" + this.groupChat.getBaseNode().toString(settings));
        } catch (Exception ex) {
            Logger.get().error(ex);
        }
    }
    
    private GenericProtocol specularShading(String type) {
        GenericProtocol shading = this.groupChat.getBaseNode().copyRef("SPECULAR_SHADING");
        shading.add("TOP_FADE_FROM", trendColor("TOP_FADE_FROM", type + "_TREND_TOP"));
        shading.add("MIDDLE_FADE_TO", trendColor("MIDDLE_FADE_TO", type + "_TREND_MIDDLE"));
        shading.add("BOTTOM_SOLID", trendColor("BOTTOM_SOLID", type + "_TREND_BOTTOM"));
        return shading;
    }
    private GenericProtocol trendColor(String node, String name) {
        GenericProtocol trend = this.groupChat.getBaseNode().copyRef(node);
        GenericProtocol color = trend.copyRef("COLOR_RGB");
        Color rgbColor = this.config.getColor(name);
        color.add("RED", (byte) rgbColor.getRed());
        color.add("GREEN", (byte) rgbColor.getGreen());
        color.add("BLUE", (byte) rgbColor.getBlue());
        trend.add("COLOR_RGB", color);
        return trend;
    }
}
