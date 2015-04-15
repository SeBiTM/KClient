package kclient.module.maumau;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.Start;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.GameConnection;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.knuddels.tools.toolbar.Button;
import kclient.module.Module;
import kclient.module.ModuleBase;

/**
 *
 * @author SeBi
 */
public class MauMauBot extends ModuleBase implements Module {
    private final Map<String, MauMauTable> tables;
    private GameConnection connection;
    
    public MauMauBot(GroupChat groupChat) {
        super(groupChat);
        super.state = true;
        this.tables = new HashMap<>();
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
    public GenericProtocol handleExtendOutput(GameConnection connection, GenericProtocol module) {
        if (connection.isMauMau())
            for (MauMauTable table : this.tables.values())
                table.handleSend(module);
        return module;
    }
    @Override
    public GenericProtocol handleExtendInput(GameConnection connection, GenericProtocol module) {
        if (this.connection == null)
            if (connection.isMauMau()) {
                this.connection = connection;
            } else {
                return module;
            }
                
        try {
            String cmd = module.getName();
            if (cmd.equals("ROOM_INIT")) {
                String gameId = module.get("GAME_ID");
                if (this.tables.containsKey(gameId))
                    this.tables.remove(gameId);
                this.tables.put(gameId, new MauMauTable(this, module));
            } else if (cmd.equals("TURN_CHANGES")) {
                String gameId = module.get("GAME_ID");
                if (this.tables.containsKey(gameId))
                    this.tables.get(gameId).turnChanges(module);
            } else if (cmd.equals("ADD_HAND_CARDS")) {
                String gameId = module.get("GAME_ID");
                if (this.tables.containsKey(gameId))
                    this.tables.get(gameId).addHandCards(module);
            } else if (cmd.equals("REMOVE_ALL_HAND_CARDS")) {
                String gameId = module.get("GAME_ID");
                if (this.tables.containsKey(gameId))
                    this.tables.get(gameId).removeHandCards();
            } else if (cmd.equals("OUT_OF_TURN_CONTROLS")) {
                String gameId = module.get("GAME_ID");
                if (this.tables.containsKey(gameId))
                    module = this.tables.get(gameId).updateControls(module);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return module;
    }

    public void sendDealy(long controllerId, final int sleep) {
        final GenericProtocol packet = this.groupChat.getExtendBaseNode().copyRef("VOID_CONTROLLER");
        packet.add("CONTROLLER_ID", controllerId);
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                }
                connection.send(packet);
            }
        }.start();
    }
    
    public GroupChat getGroupChat() {
        return this.groupChat;
    }
    public GameConnection getConnection() {
        return this.connection;
    }
    
    @Override
    public String getName() {
        return "MauMau";
    }
    @Override
    public String getAuthor() {
        return "°>_hSeBi|https://u-labs.de/members/sebi-2841/<°#_Credtis:_ °>_hpatlux|https://u-labs.de/members/patlux-4/<°";
    }
    @Override
    public String getDescription() {
        return "Das MauMau Module spielt für vollkommen automatisch MauMau ;)";
    }
    @Override
    public String getVersion() {
        return "1.0." + Start.REVISION;
    }
    @Override
    public boolean getState() {
        return this.state;
    }
    @Override
    public void setState(boolean v) {
        this.state = v;
    }
    @Override
    public List<Button> getButtons(String channel) {
        return null;
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
