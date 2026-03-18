package org.example;

/**
 * 象棋棋子類別
 * 台灣暗棋：將/帥(7) > 士/仕(6) > 象/相(5) > 車/俥(4) > 馬/傌(3) > 砲/包/炮(2) > 卒/兵(1)
 */
public class Chess {
    private final String name;   // 棋子名稱：將、士、象、車、馬、包、卒 或 帥、仕、相、俥、傌、炮、兵
    private final int weight;    // 等級權重 1-7
    private final int side;      // 0=紅方, 1=黑方
    private int row;             // 位置：列 0-3 (A-D)
    private int col;             // 位置：行 0-7 (1-8)
    private boolean revealed;    // 是否已翻開

    public Chess(String name, int weight, int side, int row, int col) {
        this.name = name;
        this.weight = weight;
        this.side = side;
        this.row = row;
        this.col = col;
        this.revealed = false;
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

    public void setPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public void setRevealed(boolean revealed) {
        this.revealed = revealed;
    }

    /** 是否為砲/包/炮（需跳過炮台吃子） */
    public boolean isCannon() {
        return weight == 2;
    }

    @Override
    public String toString() {
        return String.format("Chess{name='%s', weight=%d, side=%d, loc=(%d,%d), revealed=%s}",
                name, weight, side, row, col, revealed);
    }

    /** 取得顯示用字元（未翻開顯示 Ｘ，已翻開顯示棋子名） */
    public String toDisplayString() {
        if (!revealed) {
            return "Ｘ";
        }
        return name;
    }
}
