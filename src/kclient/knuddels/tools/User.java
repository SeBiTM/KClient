package kclient.knuddels.tools;

/**
 *
 * @author SeBi
 */
public class User {
    private final String nickname, color;
    private final int age;
    private final boolean cm, admin;
    
    public User(String nickname, String color, int age, boolean cm, boolean admin) {
        this.nickname = nickname;
        this.color = color;
        this.age = age;
        this.cm = cm;
        this.admin = admin;
    }
    
    public String getNickname() {
        return this.nickname;
    }
    public String getColor() {
        return this.color;
    }
    
    public int getAge() {
        return this.age;
    }
    
    public boolean isCM() {
        return this.cm;
    }
    public boolean isAdmin() {
        return this.admin;
    }
}
