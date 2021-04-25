package apryraz.eworld;

/**
 * Class that represents a position of EnvelopeWolrdEnv.
 */
public class Position {

    public int x, y;
    /**
     * @param row row
     * @param col col
     * Position of EnvelopeWorldEnv
     **/

    public Position(int row, int col) {
        x = row;
        y = col;
    }

    /**
     * Check if given Position is on the top of the current position.
     * @param pos The given position.
     * @return True if the given position is on top of the current position. Otherwise, false.
     */
    public boolean isOnTop(Position pos) {

        return (x + 1 == pos.x && y == pos.y) ||
                (x + 1 == pos.x && y + 1 == pos.y) ||
                (x + 1 == pos.x && y - 1 == pos.y);
    }

    /**
     * Check if given Position is on the bottom of the current position.
     * @param pos The given position.
     * @return True if the given position is on the bottom of the current position. Otherwise, false.
     */
    public boolean isOnBot(Position pos) {
        return (x - 1 == pos.x && y == pos.y) ||
                (x - 1 == pos.x && y + 1 == pos.y) ||
                (x - 1 == pos.x && y - 1 == pos.y);
    }

    /**
     * Check if given Position is on the right of the current position.
     * @param pos The given position.
     * @return True if the given position is on right of the current position. Otherwise, false.
     */
    public boolean isOnRight(Position pos) {
        return (x + 1 == pos.x && y + 1 == pos.y) ||
                (x == pos.x && y + 1 == pos.y) ||
                (x - 1 == pos.x && y + 1 == pos.y);
    }

    /**
     * Check if given Position is on the left of the current position.
     * @param pos The given position.
     * @return True if the given position is on left of the current position. Otherwise, false.
     */
    public boolean isOnLeft(Position pos) {
        return (x + 1 == pos.x && y - 1 == pos.y) ||
                (x == pos.x && y - 1 == pos.y) ||
                (x - 1 == pos.x && y - 1 == pos.y);
    }

    /**
     * Check if given Position is on the same site as the current position.
     * @param pos The given position.
     * @return True if the given position is on the same site as the current position. Otherwise, false.
     */
    public boolean isOnSite(Position pos) {
        return x == pos.x && y == pos.y;
    }

}
