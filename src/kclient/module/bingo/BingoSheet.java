package kclient.module.bingo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.knuddels.GroupChat;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.module.bingo.tools.BingoField;
import kclient.module.bingo.tools.BingoFieldState;
import kclient.module.bingo.tools.BingoSheetState;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class BingoSheet {
    private final BingoBot process;
    private final long sheetId;
    private final byte matrixSize;
    private final Map<Integer, BingoField> fields;
    private BingoSheetState state;
    private int bingoRound;
    private boolean oneToBingo;
    private final List<Integer> markFields;
    private boolean bingoCalled;
    private final GroupChat groupChat;
    
    public BingoSheet(GroupChat groupChat, BingoBot process, long sheetId, GenericProtocol sheet) {
        this.process = process;
        this.process.sheets++;
        this.sheetId = sheetId;
        this.fields = new HashMap<>();
        this.groupChat = groupChat;
        this.markFields = new ArrayList<>();
        this.matrixSize = sheet.get("BINGO_SHEET_MATRIX_SIZE");
        this.state = BingoSheetState.parse((byte)sheet.get("BINGO_SHEET_STATE_CONST"));
        
        ArrayList numbers = sheet.get("BINGO_FIELD");
        ArrayList states = sheet.get("BINGO_FIELD_STATES");
        for (int i = 0; i < numbers.size(); i++)
            this.fields.put(i, new BingoField(i, (short)numbers.get(i), (byte)states.get(i)));
    }

    public void handleUpdate(GenericProtocol sheetUpdate) {
        if (this.state != BingoSheetState.ACTIVE)
            return;
        ArrayList fieldUpdate = sheetUpdate.get("BINGO_FIELD_UPDATE");
        for (Object fu : fieldUpdate) {
            GenericProtocol field = (GenericProtocol)fu;
            int index = field.get("INDEX");
            short number = field.get("BINGO_FIELD");
            this.fields.remove(index);
            byte fstate = (byte)field.get("BINGO_FIELD_STATES");
            this.fields.put(index, new BingoField(index, number, fstate));
            if (fstate == 2 || fstate == 3)
                this.markFields.add(index);
            //System.out.println("[Sheet: " + this.sheetId + "] FieldUpdate -> " + index + ", " + getField(index).getNumber() + ", " + getField(index).getState());
        }
        //game message
        ArrayList tmpMessages = sheetUpdate.get("BINGO_GAME_MESSAGE");
        for (Object tm : tmpMessages) {
            GenericProtocol msg = (GenericProtocol)tm;
            int msgId = msg.get("MES_ID");
            String msgText = msg.get("TEXT");
            
            //System.out.println("[Sheet: " + this.sheetId + "] Message -> " + msgId + " = " + msgText);
            if (msgText.contains("noch ein Feld"))
                this.oneToBingo = true;
        }
        if (this.oneToBingo) {
            if (checkForBingo())
                this.callBingo();
        }
    }
    public void handleHistoryUpdate(GenericProtocol update) {
        if (this.state != BingoSheetState.ACTIVE)
            return;
        
        this.bingoRound = update.get("BINGO_ROUND");
        this.process.rounds++;
        if (this.bingoRound >= 100)
            this.callBingo();
        String bingoCalledNumber = update.get("BINGO_CALLED_NUMBER");
        int length = getSize();
        if (bingoCalledNumber.contains("bonus")) {
            for (int i = 0; i < length; i++) {
                int ind = this.getJokerIndex();
                if (this.markField(this.getField(ind)))
                    break;
            }
        } else {
            int number = Integer.parseInt(bingoCalledNumber);
            for (int i = 0; i < length; i++) {
                BingoField[] index = null;
                for (int j = 0; j < length; j++) {
                    index = this.getFieldsByNumber(number);
                    if ((index[0] != null && index[1] != null) && 
                            ((!this.markFields.contains(index[0].getIndex())) && 
                            (!this.markFields.contains(index[1].getIndex())))) {
                        break;
                    }
                }
                if (index == null || index[0] == null || index[1] == null)
                    continue;
                int bestIndex = this.getBestIndex(index);
                //System.out.println("[Sheet: " + this.sheetId + "] Available Fields: BestIndex: " + bestIndex + " | [0]" + index[0] + ", [1]" + index[1]);
                if (this.markField(index[bestIndex]))
                    break;
            }
        }
    }
    
    public long getId() {
        return this.sheetId;
    }
    public boolean getBingoCalled() {
        return this.bingoCalled;
    }
    public BingoSheetState getState() {
        return this.state;
    }
    public void setState(BingoSheetState state) {
        this.state = state;
    }
    
    private void callBingo() {
        if (this.state != BingoSheetState.ACTIVE)
            return;
        if (this.bingoCalled) {
            this.state = BingoSheetState.NO_BINGO;
            return;
        }
        this.bingoCalled = true;
        this.groupChat.sendPublicDelay(this.process.getChannel(), String.format("/bingo bingo %s", this.sheetId), Util.rnd(this.process.waitTimeSendBingoMin, this.process.waitTimeSendBingoMax));
    }
    private boolean markField(BingoField field) {
        if (this.state != BingoSheetState.ACTIVE)
            return true;
        if (this.markFields.contains(field.getIndex()))
            return false;
        this.markFields.add(field.getIndex());
        this.groupChat.sendPublicDelay(this.process.getChannel(), String.format("/bingo mark %s %s", this.sheetId, field.getIndex()), Util.rnd(this.process.waitTimeSendFieldMin, this.process.waitTimeSendFieldMax));
        return true;
    }
    
    private boolean isSelected(int index) {
        BingoField field = getField(index);
        if (field == null)
            return false;
        return field.getState() == BingoFieldState.SELECTED;
    }
    private int getBestIndex(BingoField[] field) {
        int index1 = field[0].getIndex();
        int index2 = field[1].getIndex();
        
        if (isSelected(index1 - 1)) //Left
            return 0;
        if (isSelected(index1 + 1)) //Right
            return 0;
        if (isSelected(index1 - this.matrixSize)) //Top
            return 0;
        if (isSelected(index1 + this.matrixSize)) //Bottom
            return 0;
        if (isSelected(index1 - (this.matrixSize + 1))) //Dia. Top Left
            return 0;
        if (isSelected(index1 - (this.matrixSize - 1))) //Dia. Top Right
            return 0;
        if (isSelected(index1 + (this.matrixSize - 1))) //Dia. Bottom Left
            return 0;
        if (isSelected(index1 + (this.matrixSize + 1))) //Dia. Bottom Right
            return 0;
        
        if (isSelected(index2 - 1)) //Left
            return 1;
        if (isSelected(index2 + 1)) //Right
            return 1;
        if (isSelected(index2 - this.matrixSize)) //Top
            return 1;
        if (isSelected(index2 + this.matrixSize)) //Bottom
            return 1;
        if (isSelected(index2 - (this.matrixSize + 1))) //Dia. Top Left
            return 1;
        if (isSelected(index2 - (this.matrixSize - 1))) //Dia. Top Right
            return 1;
        if (isSelected(index2 + (this.matrixSize - 1))) //Dia. Bottom Left
            return 1;
        if (isSelected(index2 + (this.matrixSize + 1))) //Dia. Bottom Right
            return 1;
        
        return Util.rnd(0, 1);
    }
    
    public int getSize() {
        return this.fields.size();
    }
    public BingoField getField(int index) {
        if (index >= 0 && index <= getSize())
            return this.fields.get(index);
        return null;
    }
    public BingoField getFieldByNumber(int number, int index) {
        for (int i = index; i < this.getSize(); i++) {
            if (getField(i).getNumber() == number)
                return getField(i);
        }
        return null;
    }
    public BingoField[] getFieldsByNumber(int number) {
        BingoField[] f = new BingoField[2];
        for (int i = 0; i < this.getSize(); i++) {
            if (getField(i) != null && getField(i).getNumber() == number)
                f[f[0] == null ? 0 : 1] = getField(i);
        }
        return f;
    }
    
    private int getJokerIndex() {
        int index = getDiagonalJokerIndex();
            System.out.println("Diagonal Joker: "+ index +  " - "  + (index == -1));
        
        if (index == -1) {
            index = getHorizontalJokerIndex();
            System.out.println("Horizintal Joker: "+ index +  " - "  + (index == -1));
        }
        
        if (index == -1) {
            index = getVerticalJokerIndex();
            System.out.println("Vertical Joker: "+ index +  " - " + (index == -1));
        }
        
        if (index == -1) {
            index = getRandomJokerIndex();
            System.out.println("Random Joker: "+ index +  " - " + (index == -1));
        }
        
        return getField(index).getNumber();
    }
    private int getHorizontalJokerIndex() {
        for (int i = 0; i < getSize(); i++) {
            if (isSelected(i)) {
                if (isSelected(i + 2) && isSelected(i + 3) && isSelected(i + 4) && isSelected(i + 5) && isSelected(i + 6) && !isSelected(i + 1))
                    return i + 1;
                if (isSelected(i + 1) && isSelected(i + 3) && isSelected(i + 4) && isSelected(i + 5) && isSelected(i + 6) && !isSelected(i + 2))
                    return i + 2;
                if (isSelected(i + 1) && isSelected(i + 2) && isSelected(i + 4) && isSelected(i + 5) && isSelected(i + 6) && !isSelected(i + 3))
                    return i + 3;
                if (isSelected(i + 1) && isSelected(i + 2) && isSelected(i + 3) && isSelected(i + 5) && isSelected(i + 6) && !isSelected(i + 4))
                    return i + 4;
                if (isSelected(i + 1) && isSelected(i + 2) && isSelected(i + 3) && isSelected(i + 4) && isSelected(i + 6) && !isSelected(i + 5))
                    return i + 5;
                if (isSelected(i + 1) && isSelected(i + 2) && isSelected(i + 3) && isSelected(i + 4) && isSelected(i + 5) && !isSelected(i + 6))
                    return i + 6;
            }
        }
        return -1;
    }
    private int getVerticalJokerIndex() {
        for (int i = 0; i < getSize(); i += this.matrixSize) {
            if (isSelected(i)) {
                if (isSelected(i + this.matrixSize) && 
                        isSelected(i + (this.matrixSize * 2)) && 
                        isSelected(i + (this.matrixSize * 3)) && !isSelected(i + (this.matrixSize * 4)))
                    return i + (this.matrixSize * 4);
                if (isSelected(i + this.matrixSize) && 
                        isSelected(i + (this.matrixSize * 2)) && 
                        isSelected(i + (this.matrixSize * 4)) && !isSelected(i + (this.matrixSize * 3)))
                    return i + (this.matrixSize * 3);
                if (isSelected(i + this.matrixSize) && 
                        isSelected(i + (this.matrixSize * 4)) && 
                        isSelected(i + (this.matrixSize * 3)) && !isSelected(i + (this.matrixSize * 2)))
                    return i + (this.matrixSize * 2);
                if (isSelected(i + (this.matrixSize * 4)) && 
                        isSelected(i + (this.matrixSize * 2)) && 
                        isSelected(i + (this.matrixSize * 3)) && !isSelected(i + this.matrixSize))
                    return i + (this.matrixSize);
            }
        }
        return -1;
    }
    private int getDiagonalJokerIndex() {
        for (int i = 0; i < getSize(); i += this.matrixSize + 1) {
            BingoField field = getField(i);
            if (field.getState() == BingoFieldState.SELECTED) {
                try {
                    int tmp = i + this.matrixSize + 1;
                    if (getField(tmp + 1).getState() == BingoFieldState.NOT_SELECTED && getField(tmp + 2).getState() == BingoFieldState.SELECTED &&
                            getField(tmp + 3).getState() == BingoFieldState.SELECTED && getField(tmp + 4).getState() == BingoFieldState.SELECTED && 
                            getField(tmp + 5).getState() == BingoFieldState.SELECTED && getField(tmp + 6).getState() == BingoFieldState.SELECTED) {

                        return tmp + 1;
                    } else if (getField(tmp + 1).getState() == BingoFieldState.SELECTED && getField(tmp + 2).getState() == BingoFieldState.NOT_SELECTED &&
                            getField(tmp + 3).getState() == BingoFieldState.SELECTED && getField(tmp + 4).getState() == BingoFieldState.SELECTED && 
                            getField(tmp + 5).getState() == BingoFieldState.SELECTED && getField(tmp + 6).getState() == BingoFieldState.SELECTED) {

                        return tmp + 2;
                    } else if (getField(tmp + 1).getState() == BingoFieldState.SELECTED && getField(tmp + 2).getState() == BingoFieldState.SELECTED &&
                            getField(tmp + 3).getState() == BingoFieldState.NOT_SELECTED && getField(tmp + 4).getState() == BingoFieldState.SELECTED && 
                            getField(tmp + 5).getState() == BingoFieldState.SELECTED && getField(tmp + 6).getState() == BingoFieldState.SELECTED) {

                        return tmp + 3;
                    } else if (getField(tmp + 1).getState() == BingoFieldState.SELECTED && getField(tmp + 2).getState() == BingoFieldState.SELECTED &&
                            getField(tmp + 3).getState() == BingoFieldState.SELECTED && getField(tmp + 4).getState() == BingoFieldState.NOT_SELECTED && 
                            getField(tmp + 5).getState() == BingoFieldState.SELECTED && getField(tmp + 6).getState() == BingoFieldState.SELECTED) {

                        return tmp + 4;
                    } else if (getField(tmp + 1).getState() == BingoFieldState.SELECTED && getField(tmp + 2).getState() == BingoFieldState.SELECTED &&
                            getField(tmp + 3).getState() == BingoFieldState.SELECTED && getField(tmp + 4).getState() == BingoFieldState.SELECTED && 
                            getField(tmp + 5).getState() == BingoFieldState.NOT_SELECTED && getField(tmp + 6).getState() == BingoFieldState.SELECTED) {

                        return tmp + 5;
                    } else if (getField(tmp + 1).getState() == BingoFieldState.SELECTED && getField(tmp + 2).getState() == BingoFieldState.SELECTED &&
                            getField(tmp + 3).getState() == BingoFieldState.SELECTED && getField(tmp + 4).getState() == BingoFieldState.SELECTED && 
                            getField(tmp + 5).getState() == BingoFieldState.SELECTED && getField(tmp + 6).getState() == BingoFieldState.NOT_SELECTED) {

                        return tmp + 6;
                    }
                } catch (Exception e) {
                }
            }
        }
        return -1;
    }
    private int getRandomJokerIndex() {
        for (int i = 0; i < getSize(); i++) {
            if (getField(i).getState() == BingoFieldState.NOT_SELECTED)
                return i;
        }
        return -1;
    }
    
    private boolean checkForBingo() {
        boolean found = false;
        for (int i = 0; i < getSize(); i++) {
            found = checkHorizontalForward(i);
            if (!found)
                found = checkVerticalTopBottom(i);
            if (!found)
                found = checkDiagonalLeftRight(i);
            if (!found)
                found = checkDiagonalRightLeft(i);
            if (found)
                break;
        }
        return found;
    }
    private boolean checkHorizontalForward(int index) {
        int count = 0;
        for (int i = index; i < this.getSize(); i++) {
            if ((i - index) == this.matrixSize - 1)
                return false;
            if (getField(i).getState() == BingoFieldState.SELECTED) {
                count++;
                if (count >= 7)
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }
    private boolean checkVerticalTopBottom(int index) {
        int count = 0;
        for (int i = index; i < this.getSize(); i += this.matrixSize) {
            if (getField(i).getState() == BingoFieldState.SELECTED) {
                count++;
                if (count >= 5)
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }
    private boolean checkDiagonalLeftRight(int index) {
        int count = 0;
        for (int i = index; i < this.getSize(); i += (this.matrixSize + 1)) {
            if (getField(i).getState() == BingoFieldState.SELECTED) {
                count++;
                if (count >= 7)
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }
    private boolean checkDiagonalRightLeft(int index) {
        int count = 0;
        for (int i = index; i > 0; i -= (this.matrixSize + 1)) {
            if (getField(i).getState() == BingoFieldState.SELECTED) {
                count++;
                if (count >= 7)
                    return true;
            } else {
                return false;
            }
        }
        return false;
    }
}
