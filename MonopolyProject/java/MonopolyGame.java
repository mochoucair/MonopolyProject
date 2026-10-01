import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/** Console Monopoly game. Run with: java MonopolyGame */
public class MonopolyGame {
    private static final String SAVE_FILE = "monopoly-save.dat";
    private static final Random RANDOM = new Random();
    private final Scanner scanner = new Scanner(System.in);
    private GameState state;

    public static void main(String[] args) {
        new MonopolyGame().start();
    }

    private void start() {
        System.out.println("=== MINI MONOPOLY ===");
        System.out.println("1. New game\n2. Load saved game");
        String choice = prompt("Choose 1 or 2: ");
        if (choice.equals("2")) {
            if (!loadGame()) newGame();
        } else newGame();
        play();
    }

    private void newGame() {
        Board board = new Board();
        int count = readInt("Number of players (2-4): ", 2, 4);
        for (int i = 1; i <= count; i++) {
            String name;
            while (true) {
                name = prompt("Player " + i + " name: ").trim();
                if (name.isEmpty()) { System.out.println("Name cannot be blank."); continue; }
                boolean duplicate = false;
                for (Player existing : board.getPlayers()) {
                    if (existing.getName().equalsIgnoreCase(name)) duplicate = true;
                }
                if (!duplicate) break;
                System.out.println("Choose a name that no other player is using.");
            }
            board.addPlayer(new Player(name));
        }
        state = new GameState(board);
    }

    private void play() {
        while (activePlayers().size() > 1) {
            Player player = currentPlayer();
            printStatus();
            System.out.println("\n" + player.getName() + "'s turn. Press Enter to roll, S to save, or L to load.");
            String command = prompt("> ").trim();
            if (command.equalsIgnoreCase("S")) { saveGame(); continue; }
            if (command.equalsIgnoreCase("L")) { loadGame(); continue; }
            takeTurn(player);
        }
        List<Player> survivors = activePlayers();
        if (!survivors.isEmpty()) System.out.println("\n" + survivors.get(0).getName() + " wins! 🎉");
        System.out.println("Thanks for playing.");
    }

    private void takeTurn(Player player) {
        int consecutiveDoubles = 0;
        boolean rollAgain;
        do {
            rollAgain = false;
            int die1 = RANDOM.nextInt(6) + 1;
            int die2 = RANDOM.nextInt(6) + 1;
            boolean doubles = die1 == die2;
            System.out.println(player.getName() + " rolled " + die1 + " + " + die2 + " = " + (die1 + die2));
            if (doubles) consecutiveDoubles++; else consecutiveDoubles = 0;

            if (consecutiveDoubles == 3) {
                state.getBoard().sendToJail(player);
                System.out.println("Three doubles in a row! Go directly to jail; pay $50. You do not pass GO.");
                checkBankruptcy(player);
                break;
            }

            boolean passedGo = state.getBoard().movePlayer(player, die1 + die2);
            Board.Space landed = state.getBoard().getSpace(player.getLocation());
            if (passedGo) System.out.println("Passed GO. Collect $200.");
            System.out.println("Landed on " + landed.getName() + ".");

            if (landed.sendsToJail()) {
                state.getBoard().sendToJail(player);
                consecutiveDoubles = 0;
                System.out.println("Go directly to jail; pay $50.");
            } else if (landed.getTax() > 0) {
                player.pay(landed.getTax());
                System.out.println("Pay $" + landed.getTax() + " tax.");
            } else if (landed.getProperty() != null) {
                handleProperty(player, landed.getProperty());
            }
            checkBankruptcy(player);
            if (doubles && player.isActive() && !landed.sendsToJail()) {
                rollAgain = true;
                System.out.println("Doubles! Roll again.");
            }
        } while (rollAgain && activePlayers().size() > 1);
        advanceTurn();
    }

    private void handleProperty(Player player, Property property) {
        if (!property.isOwned()) {
            System.out.println(property.getName() + " is unowned. Price: $" + property.getPurchasePrice()
                    + ", rent: $" + property.getRentPrice() + ".");
            if (player.getMoney() >= property.getPurchasePrice()
                    && prompt("Buy it? (y/n): ").trim().equalsIgnoreCase("y")) {
                player.pay(property.getPurchasePrice());
                property.setOwner(player);
                System.out.println(player.getName() + " bought " + property.getName() + ".");
            }
        } else if (property.getOwner() == player) {
            System.out.println("You own this property. No rent due.");
        } else {
            Player owner = property.getOwner();
            player.pay(property.getRentPrice());
            owner.addMoney(property.getRentPrice());
            System.out.println("Pay $" + property.getRentPrice() + " rent to " + owner.getName() + ".");
        }
    }

    private void checkBankruptcy(Player player) {
        if (player.getMoney() <= 0 && player.isActive()) {
            player.setActive(false);
            state.getBoard().returnProperties(player);
            System.out.println(player.getName() + " ran out of money. Their properties are unowned again.");
        }
    }

    private void advanceTurn() {
        List<Player> players = state.getBoard().getPlayers();
        int index = state.getCurrentPlayerIndex();
        do {
            index = (index + 1) % players.size();
            if (index == 0) state.incrementRound();
        } while (!players.get(index).isActive());
        state.setCurrentPlayerIndex(index);
    }

    private void printStatus() {
        System.out.println("\n--- Round " + state.getRound() + " ---");
        for (Player player : state.getBoard().getPlayers()) {
            Board.Space space = state.getBoard().getSpace(player.getLocation());
            System.out.printf("%-16s $%4d  %-24s %s%n", player.getName(), player.getMoney(),
                    space.getName(), player.isActive() ? "" : "BANKRUPT");
        }
    }

    private List<Player> activePlayers() {
        List<Player> active = new ArrayList<>();
        for (Player player : state.getBoard().getPlayers()) if (player.isActive()) active.add(player);
        return active;
    }

    private Player currentPlayer() {
        return state.getBoard().getPlayers().get(state.getCurrentPlayerIndex());
    }

    private void saveGame() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            out.writeObject(state);
            System.out.println("Game saved to " + SAVE_FILE + ".");
        } catch (Exception e) {
            System.out.println("Could not save the game: " + e.getMessage());
        }
    }

    private boolean loadGame() {
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(SAVE_FILE))) {
            state = (GameState) in.readObject();
            System.out.println("Saved game loaded.");
            return true;
        } catch (Exception e) {
            System.out.println("No readable saved game found (" + e.getMessage() + ").");
            return false;
        }
    }

    private String prompt(String message) {
        System.out.print(message);
        return scanner.nextLine();
    }

    private int readInt(String message, int minimum, int maximum) {
        while (true) {
            try {
                int value = Integer.parseInt(prompt(message).trim());
                if (value >= minimum && value <= maximum) return value;
            } catch (NumberFormatException ignored) { }
            System.out.println("Enter a number from " + minimum + " to " + maximum + ".");
        }
    }
}
