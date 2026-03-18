package org.example;

/**
 * 玩家類別
 */
public class Player {
    private final String name;
    private int side;  // 0=紅方, 1=黑方，遊戲開始翻棋後決定

    public Player(String name) {
        this.name = name;
        this.side = -1;  // 尚未決定
    }

    public String getName() {
        return name;
    }

    public int getSide() {
        return side;
    }

    public void setSide(int side) {
        this.side = side;
    }
}
