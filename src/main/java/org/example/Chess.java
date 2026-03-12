package org.example;

/**
 * 象棋棋子類別
 * 台灣暗棋：將(7)>士(6)>象(5)>車(4)>馬(3)>包(2)>兵(1)
 */
public class Chess {
    private final String name;   // 棋子名稱：將、士、象、車、馬、包、兵
    private final int weight;    // 等級權重 1-7
    private final int side;      // 0: 紅方, 1: 黑方
    private int row;             // 位置 row 0-3
    private int col;             // 位置 col 0-7
    private boolean flipped;     // 是否已翻開

    public Chess(String name, int weight, int side, int row, int col) {
        this.name = name;
        this.weight = weight;
        this.side = side;
        this.row = row;
        this.col = col;
        this.flipped = false;
    }

    public String getName() {
        return name;
    }

    public int getWeight() {
        return weight;
    }

    public int getSide() {
        return side;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public void setLocation(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean isFlipped() {
        return flipped;
    }

    public void setFlipped(boolean flipped) {
        this.flipped = flipped;
    }

    /**
     * 取得顯示用字串（未翻開顯示 Ｘ，已翻開顯示棋子名稱）
     */
    @Override
    public String toString() {
        if (!flipped) {
            return "Ｘ";
        }
        return name;
    }

    /**
     * 取得完整資訊字串（除錯用）
     */
    public String toDetailString() {
        return String.format("%s(weight=%d,side=%d,loc=%d,%d)", name, weight, side, row, col);
    }
}
