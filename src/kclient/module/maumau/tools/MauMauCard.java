package kclient.module.maumau.tools;

import kclient.knuddels.network.generic.GenericProtocol;

/**
 *
 * @author SeBi
 */
public class MauMauCard {
    private final long id;
    private String image;
    private int number;
    private int category, millis;
    private short index;
    private GenericProtocol img1, img2;

    public MauMauCard(long id, String image, GenericProtocol img1, GenericProtocol img2, int millis, short index) {
        this.id = id;
        this.image = image;
        this.img1 = img1;
        this.img2 = img2;
        this.index = index;
        this.millis = millis;
        
        if (this.image.contains("/"))
            this.image = this.image.split("/")[1];
        
        String tmp = this.image.substring(2);
        if (tmp.contains("."))
            tmp = tmp.substring(0, tmp.indexOf("."));
        String[] args = tmp.split("-");
        if (args.length >= 2) {
            number = Integer.parseInt(args[1]);
            if (number == 11 && args.length > 2)
                category = Integer.parseInt(args[2]);
            else
                category = Integer.parseInt(args[0]);
        }
    }
    
    public GenericProtocol getImg1() {
        return this.img1;
    }
    public GenericProtocol getImg2() {
        return this.img2;
    }
    public int getMillis() {
        return this.millis;
    }
    public short getIndex() {
        return this.index;
    }
    
    public long getId() {
        return this.id;
    }
    
    public int getNumber() {
        return this.number;
    }
    public int getCategory() {
        return this.category;
    }
    public String getColor() {
        int c = getCategory();
        return (c == 1 ? "Blue" : c == 2 ? "Red" : c == 3 ? "Green" : "Yellow");
    }
    
    @Override
    public boolean equals(Object o) {
        if (o == null)
            return false;
        if (!(o instanceof MauMauCard))
            return false;
        return o.hashCode() == this.hashCode();
    }
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 37 * hash + this.number;
        hash = 37 * hash + this.category;
        return hash;
    }
    
    public boolean isBube() {
        return this.number == 11;
    }
    
    @Override
    public String toString() {
        return "{ F: " + getColor() + ", N: " + getNumber() + ", Id: " + getId() + "}";
    }
}
