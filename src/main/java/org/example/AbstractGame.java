package org.example;

/**
 * 抽象遊戲類別
 * 宣告遊戲的基本介面
 */
public abstract class AbstractGame {
    protected Player player1;
    protected Player player2;

    /**
     * 設定兩位玩家
     */
    public abstract void setPlayers(Player p1, Player p2);

    /**
     * 檢查遊戲是否結束
     */
    public abstract boolean gameOver();

    /**
     * 執行移動
     * @param location 位置編碼 (row * 8 + col)
     * @return 是否成功
     */
    public abstract boolean move(int location);
}
