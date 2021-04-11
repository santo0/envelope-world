package apryraz.eworld;

import java.util.Objects;

public class Position {
    /**
     *
     **/
    public int x, y;

    public Position(int row, int col) {
        x = row;
        y = col;
    }

    public boolean isOnRight(Position pos) {
        return (x + 1 == pos.x && y == pos.y) ||
                (x + 1 == pos.x && y + 1 == pos.y) ||
                (x + 1 == pos.x && y - 1 == pos.y);
    }

    public boolean isOnLeft(Position pos) {
        System.out.println(x + "," + y + "---" + pos.x + "," + pos.y);
        System.out.println(((x - 1 == pos.x && y == pos.y) ||
                (x - 1 == pos.x && y + 1 == pos.y) ||
                (x - 1 == pos.x && y - 1 == pos.y)) + "," + (x - 1 == pos.x && y == pos.y) + "," +
                (x - 1 == pos.x && y + 1 == pos.y) + "," +
                (x - 1 == pos.x && y - 1 == pos.y));
        return (x - 1 == pos.x && y == pos.y) ||
                (x - 1 == pos.x && y + 1 == pos.y) ||
                (x - 1 == pos.x && y - 1 == pos.y);
    }

    public boolean isOnTop(Position pos) {
        return (x + 1 == pos.x && y + 1 == pos.y) ||
                (x == pos.x && y + 1 == pos.y) ||
                (x - 1 == pos.x && y + 1 == pos.y);
    }

    public boolean isOnBot(Position pos) {
        return (x + 1 == pos.x && y - 1 == pos.y) ||
                (x == pos.x && y - 1 == pos.y) ||
                (x - 1 == pos.x && y - 1 == pos.y);
    }

    public boolean isOnSite(Position pos) {
        return x == pos.x && y == pos.y;
    }

}
