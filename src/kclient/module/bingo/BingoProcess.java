package kclient.module.bingo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.module.bingo.tools.BingoSheetState;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class BingoProcess {
    private final GroupChat groupChat;
    private final BingoBot bot;
    private final String channel;
    private final Map<Long, BingoSheet> sheets;
    
    public BingoProcess(String channel, BingoBot bot, GroupChat groupChat) {
        this.groupChat = groupChat;
        this.channel = channel;
        this.bot = bot;
        this.sheets = new HashMap<>();
    }
    
    public void fixSheetError() {
        long sheetId = -1L;
        for (BingoSheet sheet : this.sheets.values()) {
            if (sheet.getBingoCalled())
                sheetId = sheet.getId();
        }
        if (sheetId != -1L) {
            this.sheets.remove(sheetId);
            this.groupChat.removeFrame(0, sheetId);
        }
    }
    
    public void handle(GenericProtocol module) {
        if (module.getName().equals("BINGO_INIT")) {
            final GenericProtocol sheet = module.get("BINGO_SHEET");
            final long sheetId = BingoBot.getSheetId(sheet);
            if (this.sheets.containsKey(sheetId))
                this.sheets.remove(sheetId);
            new Thread("BingoSheet[" + sheetId + "]") {
                @Override
                public void run() {
                    BingoProcess.this.sheets.put(sheetId, new BingoSheet(BingoProcess.this.groupChat, BingoProcess.this, sheetId, sheet));
                }
            }.start();
        } else if (module.getName().equals("BINGO_SET_STATE")) {
            long sheetId = BingoBot.getSheetId(module);
            BingoSheetState state = BingoSheetState.parse((byte)module.get("BINGO_SHEET_STATE_CONST"));
            //System.out.println("[SetState -> " + sheetId + "] State: " + state);
            if (!this.sheets.containsKey(sheetId) || this.sheets.get(sheetId) == null) {
                return;
            }
            this.sheets.get(sheetId).setState(state);
            if (state != BingoSheetState.ACTIVE) {
                this.sheets.remove(sheetId);
                this.groupChat.removeFrame(0, sheetId);
            }
        } else if (module.getName().equals("BINGO_UPDATE")) {
            ArrayList bingoSheets = module.get("BINGO_SHEET_UPDATE");
            for (Object bso : bingoSheets) {
                GenericProtocol sheet = (GenericProtocol)bso;
                long sheetId = BingoBot.getSheetId(sheet);
                if (!this.sheets.containsKey(sheetId))
                    continue;
                this.sheets.get(sheetId).handleUpdate(sheet);
            }
            //messages
            ArrayList tmpMessages = module.get("BINGO_GAME_MESSAGE");
            for (Object tm : tmpMessages) {
                GenericProtocol msg = (GenericProtocol)tm;
                int msgId = msg.get("MES_ID");
                String msgText = msg.get("TEXT");
                if (msgText.contains("hattest _kein_ Bingo") || msgText.contains("Fehler: Das Bingo-Blatt nimmt")) {
                    long sheetId = -1L;
                    for (BingoSheet sheet : this.sheets.values()) {
                        if (sheet.getBingoCalled())
                            sheetId = sheet.getId();
                    }
                    if (sheetId != -1L) {
                        this.sheets.remove(sheetId);
                        this.groupChat.removeFrame(0, sheetId);
                    } else
                        this.sheets.clear();
                }
                //System.out.println("Global Message -> " + msgId + " = " + msgText);
            }

            //history update
            ArrayList tmpHistory = module.get("BINGO_HISTORY_UPDATE");
            for (Object th : tmpHistory) {
                GenericProtocol history = (GenericProtocol)th;
                long sheetId = BingoBot.getSheetId(history);
                if (!this.sheets.containsKey(sheetId))
                    continue;
                this.sheets.get(sheetId).handleHistoryUpdate(history);
            }
        }
    }
    
    public BingoBot getBot() {
        return this.bot;
    }
    
    public String getChannel() {
        return this.channel;
    }
    public void joinBingo() {
        if (!this.bot.getAutoJoin())
            return;
        new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    int sheetsCount = 3;
                    for (int i = 0; i < sheetsCount; i++)
                        BingoProcess.this.groupChat.sendPublicDelay(channel, "/bingo buy", Util.rnd(5000, 10000));
                } catch (InterruptedException ex) {
                }
            }
        }.start();
    }
}
