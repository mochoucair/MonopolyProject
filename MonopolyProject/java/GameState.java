import java.io.Serializable;

/** Serializable game data used by save and load. */
public class GameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Board board;
    private int currentPlayerIndex;
    private int round;

    public GameState(Board board) {
        this.board = board;
        this.currentPlayerIndex = 0;
        this.round = 1;
    }
    public Board getBoard() { return board; }
    public int getCurrentPlayerIndex() { return currentPlayerIndex; }
    public void setCurrentPlayerIndex(int index) { currentPlayerIndex = index; }
    public int getRound() { return round; }
    public void incrementRound() { round++; }
}
