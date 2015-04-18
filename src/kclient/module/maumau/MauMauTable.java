package kclient.module.maumau;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import kclient.knuddels.network.generic.GenericProtocol;
import kclient.module.maumau.tools.MauMauCard;
import kclient.tools.Util;

/**
 *
 * @author SeBi
 */
public class MauMauTable {
    private final MauMauBot bot;
    private short nicknameComponent, playerPosX, playerPosY;
    private final Map<Long, MauMauCard> handCards;
    private MauMauCard currentCard;
    private String gameId;
    private MauMauCard lastSend;
    
    public MauMauTable(MauMauBot bot, GenericProtocol protocol) {
        this.bot = bot;
        this.handCards = new HashMap<>();
        this.gameId = protocol.get("GAME_ID");
    }
 
    public void addHandCards(GenericProtocol protocol) {
        GenericProtocol handCards1 = protocol.get("ADD_HAND_CARDS_IMGS1");
        GenericProtocol handCards2 = protocol.get("ADD_HAND_CARDS_IMGS2");
        
        if (handCards2 == null)
            return;
        ArrayList images1 = handCards1.get("ZIMAGE");
        ArrayList images2 = handCards2.get("ZIMAGE");
        ArrayList timeMillis = protocol.get("TIME_MILLIS");
        ArrayList sortIndex = protocol.get("SORT_INDEX");
        for (int i = 0; i < images2.size(); i++) {
            String image = ((GenericProtocol)images2.get(i)).get("IMAGE");
            if (image.contains("/"))
                image = image.split("/")[1];
            if (!(image.contains("c_")))
                continue;
            long id = Long.parseLong((String)((GenericProtocol)((GenericProtocol)images1.get(i)).get("LEFT_CLICK_FUNCTION")).get("CLICK_MSG"));
            this.handCards.put(id, new MauMauCard(id, image, 
                    (GenericProtocol)images1.get(i), (GenericProtocol)images2.get(i), 
                    (int)timeMillis.get(i), (short)sortIndex.get(i)));
        }
    }
    public void removeHandCards() {
        this.handCards.clear();
    }
   
    public void turnChanges(GenericProtocol protocol) {
        //<editor-fold defaultstate="collapsed" desc="FILL_TEXT_LABEL">
        ArrayList fillTextLabels = protocol.get("FILL_TEXT_LABEL");
        for (Object ttl : fillTextLabels) {
            GenericProtocol textLabel = (GenericProtocol)ttl;
            String text = textLabel.get("TEXT");
            if (text.toLowerCase().equals(this.bot.getGroupChat().getNickname().toLowerCase())) {
                this.nicknameComponent = textLabel.get("COMPONENT_ID");
            }
        }
        //</editor-fold>
                
        //<editor-fold defaultstate="collapsed" desc="TEXT_LABEL">
        ArrayList textLabels = protocol.get("TEXT_LABEL");
        for (Object ttl : textLabels) {
            GenericProtocol textLabel = (GenericProtocol)ttl;
            GenericProtocol baseComponent = textLabel.get("BASE_COMPONENT");

            if (((short)baseComponent.get("COMPONENT_ID")) == this.nicknameComponent) {
                GenericProtocol position = baseComponent.get("POSITION");
                this.playerPosX = position.get("POS_X");
                this.playerPosY = position.get("POS_Y");
            }
        }
        //</editor-fold>
                
        //<editor-fold defaultstate="collapsed" desc="ZIMAGES">
        ArrayList zimages = protocol.get("ZIMAGE");
        for (Object ti : zimages) {
            GenericProtocol zimage = (GenericProtocol)ti;
            GenericProtocol baseComponent = zimage.get("BASE_COMPONENT");
            GenericProtocol position = baseComponent.get("POSITION");
            String image = zimage.get("IMAGE");
            if (image == null)
                continue;
            
            if (image.contains("timecircle_yellow")) {
                short x = position.get("POS_X");
                short y = position.get("POS_Y");
                if (this.playerPosX == x) {
                    int tmp = this.playerPosY > y ? this.playerPosY - y : y - this.playerPosY;
                    if (tmp < 80) {
                        playMau();
                    }
                }
            } else if (image.contains("c_") && image.endsWith(".gif")) {
                this.currentCard = new MauMauCard(-1, image, null, null, 0, (short)0);
                if (this.lastSend != null) {
                    if (this.currentCard.equals(this.lastSend)) {
                        if (this.handCards.containsKey(this.lastSend.getId()))
                            this.handCards.remove(this.lastSend.getId());
                        this.lastSend = null;
                    }
                }
            } else if (image.contains("wishcolor")) {
                this.wishColor(zimages);
            } else if (image.contains("button_mau.png")) {
                String id = ((GenericProtocol)((GenericProtocol)zimage).get("LEFT_CLICK_FUNCTION")).get("CLICK_MSG");
                this.bot.sendDealy(Long.parseLong(id), 500);
            }
        }
        //</editor-fold>
    }
    
    public GenericProtocol updateControls(GenericProtocol protocol) {
        ArrayList regularButtons = protocol.get("REGULAR_BUTTON");
        for (Object tb : regularButtons) {
            GenericProtocol regularButton = (GenericProtocol)tb;
            GenericProtocol fullButtonSettings = regularButton.get("FULL_BUTTON_SETTINGS");

            String btnText = fullButtonSettings.get("TEXT");
            long buttonAction = Long.parseLong((String)regularButton.get("CLICK_MSG"));
            if (btnText.equals("Mitspielen"))
                this.bot.sendDealy(buttonAction, Util.rnd(2000, 4000));
        }
        return protocol;
    }
    
    public void handleSend(GenericProtocol p) {
        if (p.getName().equals("VOID_CONTROLLER") && this.lastSend != null) {
            long id = p.get("CONTROLLER_ID");
            if (id == this.lastSend.getId()) {
                MauMauCard c = null;
                for (MauMauCard card : this.handCards.values())
                    if (card.getId() == id) {
                        c = card;
                        break;
                    }
                if (c != null) {
                    this.handCards.remove(c.getId());
                    this.fixCards();
                }
            }
        }
    }

    private String getBestColor() {
        int red = 0, green = 0, blue = 0, yellow = 0;
        for (MauMauCard c : this.handCards.values())
            switch (c.getCategory()) {
                case 1:
                    blue++;
                    break;
                case 2:
                    red++;
                    break;
                case 3:
                    green++;
                    break;
                case 4:
                    yellow++;
                    break;
            }
    
        String key = "red";
        if (green > blue && green > red && green > yellow)
            key = "green";
        else if (blue > green && blue > red && blue > yellow)
            key = "blue";
        else if (yellow > blue && yellow > red && yellow > green)
            key = "yellow";
        else if (red > blue && red > yellow && red > green)
            key = "red";
        return key;
    }
    
    private void wishColor(ArrayList images) {
        String color = getBestColor();
        for (Object ti : images) {
            GenericProtocol zimage = (GenericProtocol)ti;
            String img = zimage.get("IMAGE");
            if (img.contains("/"))
                img = img.split("/")[1];
            img = img.substring(0, img.indexOf("."));
            if (img.equals("wishcolor_" + color)) {
                String id = ((GenericProtocol)((GenericProtocol)zimage).get("LEFT_CLICK_FUNCTION")).get("CLICK_MSG");
                this.bot.sendDealy(Long.parseLong(id), Util.rnd(2000, 3000));
                break;
            }
        }
        
    }
    private void fixCards() {
        GenericProtocol remove = this.bot.getGroupChat().getExtendBaseNode().copyRef("REMOVE_ALL_HAND_CARDS");
        remove.add("GAME_ID", gameId);
        this.bot.getConnection().receive(remove);
        
        GenericProtocol addHandCards = this.bot.getGroupChat().getExtendBaseNode().copyRef("ADD_HAND_CARDS");
        addHandCards.add("GAME_ID", this.gameId);
        ArrayList zImage1 = new ArrayList<>();
        ArrayList zImage2 = new ArrayList<>();
        ArrayList timeMillis = new ArrayList<>();
        ArrayList sortIndex = new ArrayList<>();
        for (MauMauCard card : this.handCards.values()) {
            zImage1.add(card.getImg1());
            zImage2.add(card.getImg2());
            timeMillis.add(0);
            sortIndex.add(card.getIndex());
        }

        GenericProtocol imgs1 = addHandCards.copyRef("ADD_HAND_CARDS_IMGS1");
        imgs1.add("ZIMAGE", zImage1);

        GenericProtocol imgs2 = addHandCards.copyRef("ADD_HAND_CARDS_IMGS2");
        imgs2.add("ZIMAGE", zImage2);

        addHandCards.add("ADD_HAND_CARDS_IMGS1", imgs1);
        addHandCards.add("ADD_HAND_CARDS_IMGS2", imgs2);

        addHandCards.add("SORT_INDEX", sortIndex);
        addHandCards.add("TIME_MILLIS", timeMillis);
        this.bot.getConnection().receive(addHandCards);
    }
    private void playMau() {
        List<MauMauCard> checkedCards = new ArrayList<>();
        for (MauMauCard card : this.handCards.values()) {
            if (checkCard(card)) {
                checkedCards.add(card);
            }
        }
        
        MauMauCard rndCard = null;
        if (checkedCards.size() <= 0) {
            for (MauMauCard c : this.handCards.values())
                if (c.isBube()) {
                    rndCard = c;
                    break;
                }
        } else {
            if (this.currentCard.getNumber() == 7 || this.currentCard.getNumber() == 8 || this.currentCard.getNumber() == 14) {
                for (MauMauCard card : this.handCards.values())
                    if (card.getNumber() == this.currentCard.getNumber()) {
                        rndCard = card;
                        break;
                    }
            } else if (checkedCards.size() <= 0 && !this.currentCard.isBube()) {
                for (MauMauCard card : this.handCards.values())
                    if (card.isBube()) {
                        rndCard = card;
                        break;
                    }
            } else if (checkedCards.size() <= 0 && this.currentCard.isBube()) {
                for (MauMauCard card : this.handCards.values())
                    if (card.getCategory() == this.currentCard.getCategory()) {
                        rndCard = card;
                        break;
                    }
            }

            if (rndCard == null && checkedCards.size() > 0) {
                rndCard = checkedCards.get(Util.rnd(0, checkedCards.size() - 1));
            }
        }
        if (rndCard == null)
            return;
        this.bot.sendDealy(rndCard.getId(), Util.rnd(500, 1500));
        this.lastSend = rndCard;
    }
    private boolean checkCard(MauMauCard card) {
        if (this.currentCard.getNumber() == 13 && card.getNumber() == 13)
            return true;

        if (this.currentCard.isBube() && card.isBube()) 
            return false;
        
        if (this.currentCard.isBube())
            return this.currentCard.getCategory() == card.getCategory();
        if (card.getCategory() == this.currentCard.getCategory())
            return true;
        if (this.currentCard.getNumber() == card.getNumber())
            return true;
        return false;
    }
}
