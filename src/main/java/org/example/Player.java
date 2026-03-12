package org.example;

/**
 * 玩家類別
 */
public class Player {
    private final String name;
    private final int side;  // 0: 紅方, 1: 黑方

    public Player(String name, int side) {
        this.name = name;
        this.side = side;
    }

    public String getName() {
        return name;
    }

    public int getSide() {
        return side;
    }
}
