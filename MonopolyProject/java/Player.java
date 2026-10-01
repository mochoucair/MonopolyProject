import java.io.Serializable;

/** A player and their current game status. */
public class Player implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private int money = 1500;
    private int location = 0;
    private boolean active = true;

    public Player(String name) { this.name = name; }

    public String getName() { return name; }
    public int getMoney() { return money; }
    public void addMoney(int amount) { money += amount; }
    public void pay(int amount) { money -= amount; }
    public int getLocation() { return location; }
    public void setLocation(int location) { this.location = location; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
