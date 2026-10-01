import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** The 40 spaces of the board and the players moving around it. */
public class Board implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int GO = 0;
    public static final int JAIL = 10;
    public static final int GO_TO_JAIL = 30;
    public static final int GO_BONUS = 200;
    public static final int JAIL_FEE = 50;

    public static class Space implements Serializable {
        private static final long serialVersionUID = 1L;
        private final String name;
        private final Property property;
        private final int tax;
        private final boolean goToJail;

        private Space(String name, Property property, int tax, boolean goToJail) {
            this.name = name; this.property = property; this.tax = tax; this.goToJail = goToJail;
        }
        public String getName() { return name; }
        public Property getProperty() { return property; }
        public int getTax() { return tax; }
        public boolean sendsToJail() { return goToJail; }
    }

    private final List<Space> spaces = new ArrayList<>();
    private final List<Player> players = new ArrayList<>();

    public Board() {
        addSpecial("GO");
        addProperty("Mediterranean Avenue", 60, 2); addEmpty();
        addProperty("Baltic Avenue", 60, 4); addTax("Income Tax", 200);
        addProperty("Reading Railroad", 200, 25); addProperty("Oriental Avenue", 100, 6);
        addEmpty(); addProperty("Vermont Avenue", 100, 6); addProperty("Connecticut Avenue", 120, 8);
        addSpecial("Jail / Just Visiting");
        addProperty("St. Charles Place", 140, 10); addProperty("Electric Company", 150, 12);
        addProperty("States Avenue", 140, 10); addProperty("Virginia Avenue", 160, 12);
        addProperty("Pennsylvania Railroad", 200, 25); addProperty("St. James Place", 180, 14);
        addEmpty(); addProperty("Tennessee Avenue", 180, 14); addProperty("New York Avenue", 200, 16);
        addSpecial("Free Parking");
        addProperty("Kentucky Avenue", 220, 18); addEmpty(); addProperty("Indiana Avenue", 220, 18);
        addProperty("Illinois Avenue", 240, 20); addProperty("B. & O. Railroad", 200, 25);
        addProperty("Atlantic Avenue", 260, 22); addProperty("Ventnor Avenue", 260, 22);
        addProperty("Water Works", 150, 12); addProperty("Marvin Gardens", 280, 24);
        spaces.add(new Space("Go To Jail", null, 0, true));
        addProperty("Pacific Avenue", 300, 26); addProperty("North Carolina Avenue", 300, 26);
        addEmpty(); addProperty("Pennsylvania Avenue", 320, 28);
        addProperty("Short Line Railroad", 200, 25); addEmpty();
        addProperty("Park Place", 350, 35); addTax("Luxury Tax", 100);
        addProperty("Boardwalk", 400, 50);
    }

    private void addProperty(String name, int price, int rent) {
        spaces.add(new Space(name, new Property(name, price, rent), 0, false));
    }
    private void addTax(String name, int amount) { spaces.add(new Space(name, null, amount, false)); }
    private void addEmpty() { spaces.add(new Space("Open Space", null, 0, false)); }
    private void addSpecial(String name) { spaces.add(new Space(name, null, 0, false)); }

    public void addPlayer(Player player) { players.add(player); }
    public List<Player> getPlayers() { return players; }
    public int size() { return spaces.size(); }
    public Space getSpace(int location) { return spaces.get(location); }
    public List<Space> getSpaces() { return spaces; }

    /** Moves clockwise. Returns true when the player passed GO. */
    public boolean movePlayer(Player player, int spacesToMove) {
        int oldLocation = player.getLocation();
        int unwrapped = oldLocation + spacesToMove;
        player.setLocation(unwrapped % spaces.size());
        boolean passedGo = unwrapped >= spaces.size();
        if (passedGo) player.addMoney(GO_BONUS);
        return passedGo;
    }

    public void sendToJail(Player player) {
        player.setLocation(JAIL);
        player.pay(JAIL_FEE);
    }

    public void returnProperties(Player player) {
        for (Space space : spaces) {
            Property property = space.getProperty();
            if (property != null && property.getOwner() == player) property.setOwner(null);
        }
    }

    public int countProperties(Player player) {
        int count = 0;
        for (Space space : spaces) {
            if (space.getProperty() != null && space.getProperty().getOwner() == player) count++;
        }
        return count;
    }
}
