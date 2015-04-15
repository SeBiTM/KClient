package kclient.module.stayonline;

import java.util.Arrays;
import java.util.List;
import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.tools.toolbar.Button;
import kclient.module.Module;
import kclient.module.ModuleBase;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class StayOnlineBot extends ModuleBase implements Module {

    public StayOnlineBot(GroupChat groupChat) {
        super(groupChat);
        super.state = false;
    }
    
    @Override
    public String getName() {
        return "StayOnline";
    }
    @Override
    public String getAuthor() {
        return "°>_hSeBi|https://u-labs.de/members/sebi-2841/<°";
    }
    @Override
    public String getDescription() {
        return "Das StayOnline Module hält deinen Nick online und sammelt so für dich Minuten.##"
                + "°>bullet2.png<° Kann über die Toolbar de/aktiviert werden.#"
                + "°>bullet2.png<° Alternativ kannst du mit _/mdl +|- " + getName() + "_ den Bot aktivieren|deaktivieren.#"
                + "°>bullet2.png<° Sendet alle 3 Minuten eine Random Zahl an " + this.groupChat.getButlerName();
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
    }

    @Override
    public void setState(boolean v) {
        super.setState(v);
        if (v) {
            new Thread("StayOnline") {
                @Override
                public void run() {
                    while (state) {
                        try {
                            Thread.sleep((60 * 3) * 1000);
                        } catch (InterruptedException e) {
                        }
                        groupChat.sendPublic(groupChat.getCurrentChannel(), "/p " + groupChat.getButlerName() + ":" + Util.rnd(1, 99999));
                    }
                }
            }.start();
        }
    }
    
    @Override
    public List<Button> getButtons(String channel) {
        return Arrays.asList(new Button[] {
            new Button("StayOnline", "py_" + (super.state ? "g" : "r") + ".gif", "/mdl " + (super.state ? "-" : "+") + getName(), false)
        });
    }

    @Override
    public String handleInput(String packet, String[] tokens) {
        return packet;
    }
    @Override
    public String handleOutput(String packet, String[] tokens) {
        return packet;
    }

    @Override
    public GenericProtocol handleExtendInput(GameConnection connection, GenericProtocol protocol) {
        return protocol;
    }
    @Override
    public GenericProtocol handleExtendOutput(GameConnection connection, GenericProtocol protocol) {
        return protocol;
    }

    @Override
    public boolean handleCommand(String cmd, String arg, String channel) {
        return true;
    }

    @Override
    public void save() {
    }
    @Override
    public void load() {
    }
    
}
