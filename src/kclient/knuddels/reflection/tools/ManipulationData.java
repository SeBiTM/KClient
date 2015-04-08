package kclient.knuddels.reflection.tools;

import kclient.tools.Parameter;

/**
 *
 * @author SeBi
 */
public class ManipulationData {
    private final Parameter params;
    
    public ManipulationData(String file) {
        this.params = new Parameter(file);
    }
    
    public String getGroupChat() {
        return this.params.get("groupchat_class");
    }
    public String getGroupChatInput() {
        return this.params.get("groupchat_input");
    }
    public String getGroupChatOutput() {
        return this.params.get("groupchat_output");
    }
    public String getGroupChatMdlInput() {
        return this.params.get("groupchat_module_input");
    }
    public String getGroupChatChannels() {
        return this.params.get("groupchat_get_channels");
    }
    public String getGroupChatModule() {
        return this.params.get("groupchat_get_module");
    }

    public String getModuleClass() {
        return this.params.get("module_class");
    }
    public String getModuleParent() {
        return this.params.get("module_parent");
    }
    public String getModuleReset() {
        return this.params.get("module_reset");
    }
    public String getModuleRead() {
        return this.params.get("module_read");
    }
    public String getModuleWrite() {
        return this.params.get("module_write");
    }
    
    public String getChannelClass() {
        return this.params.get("channel_class");
    }

    public String getConnectionClass() {
        return this.params.get("connection_class");
    }
    public String getConnectionOutput() {
        return this.params.get("connection_output");
    }
    public String getConnectionField() {
        return this.params.get("connection_field");
    }
    public String getConnectionFieldInput() {
        return this.params.get("connection_field_input");
    }
    public String getConnectionConnect() {
        return this.params.get("connection_connect");
    }
    public String getConnectionConnected() {
        return this.params.get("connection_connected");
    }
    public String getConnectionModule() {
        return this.params.get("connection_module");
    }
    public String getConnectionModuleCheck() {
        return this.params.get("connection_module_check");
    }
    public String getConnectionRead() {
        return this.params.get("connection_read");
    }
    public String getConnectionStream() {
        return this.params.get("connection_stream");
    }
    public String getConnectionClose() {
        return this.params.get("connection_close");
    }
    
    public String getGameHandlerClass() {
        return this.params.get("game_handler_class");
    }
    public String getGameHandlerField() {
        return this.params.get("game_handler_field");
    }
    
    public String getBingoFrameClass() {
        return this.params.get("bingo_frame_class");
    }
    public String getBingoFrameGroupChat() {
        return this.params.get("bingo_frame_groupchat");
    }

}
