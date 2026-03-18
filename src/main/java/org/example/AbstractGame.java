package org.example;

/**
 * 抽象遊戲類別
 */
public abstract class AbstractGame {
    protected Player player1;
    protected Player player2;

    public abstract void setPlayers(Player player1, Player player2);
    public abstract boolean gameOver();
    /** location 編碼：fromRow*1000 + fromCol*100 + toRow*10 + toCol */
    public abstract boolean move(int location);
}
