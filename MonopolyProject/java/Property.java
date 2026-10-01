import java.io.Serializable;

/** A buyable space on the board. */
public class Property implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String name;
    private final int purchasePrice;
    private final int rentPrice;
    private Player owner;

    public Property(String name, int purchasePrice, int rentPrice) {
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.rentPrice = rentPrice;
        this.owner = null;
    }

    public String getName() { return name; }
    public int getPurchasePrice() { return purchasePrice; }
    public int getRentPrice() { return rentPrice; }
    public Player getOwner() { return owner; }
    public void setOwner(Player owner) { this.owner = owner; }
    public boolean isOwned() { return owner != null; }
}
