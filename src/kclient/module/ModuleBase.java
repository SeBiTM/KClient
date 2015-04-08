package kclient.module;

import kclient.knuddels.GroupChat;

/**
 *
 * @author SeBi
 */
public class ModuleBase {
    protected GroupChat groupChat;
    protected boolean state;
    
    public ModuleBase(GroupChat groupChat) {
        this.groupChat = groupChat;
        this.state = true;
    }
    
    public boolean getState() { return this.state; }
    public void setState(boolean v) { this.state = v; }
}
