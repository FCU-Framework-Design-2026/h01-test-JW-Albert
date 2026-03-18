package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * 台灣暗棋遊戲
 * 棋盤 4x8，棋子等級：將/帥(7) > 士/仕(6) > 象/相(5) > 車/俥(4) > 馬/傌(3) > 砲/包/炮(2) > 卒/兵(1)
 * 砲需跳過炮台吃子；將帥不可吃兵卒；兵卒可吃將帥，不可吃砲
 */
public class ChessGame extends AbstractGame {
    private static final int ROWS = 4;
    private static final int COLS = 8;
    private static final String[] RED_NAMES = {"帥", "仕", "相", "俥", "傌", "炮", "兵"};
    private static final String[] BLACK_NAMES = {"將", "士", "象", "車", "馬", "包", "卒"};
    private static final int[] WEIGHTS = {7, 6, 5, 4, 3, 2, 1};  // 對應上述順序

    private Chess[][] board;
    private List<Chess> allChess;
    private int currentPlayer;  // 0 或 1，對應 player1/player2
    private boolean gameStarted;  // 是否已翻出第一子決定雙方陣營
    private Integer selectedRow, selectedCol;  // 已選中的棋子位置（待輸入目的地時使用）
    private final Random random = new Random();
    private final Scanner scanner = new Scanner(System.in);

    public ChessGame() {
        this.board = new Chess[ROWS][COLS];
        this.allChess = new ArrayList<>();
        this.currentPlayer = 0;
        this.gameStarted = false;
        this.selectedRow = null;
        this.selectedCol = null;
    }

    /** 產生 32 個棋子並隨機擺放 */
    public void generateChess() {
        allChess.clear();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                board[r][c] = null;
            }
        }

        // 標準 32 子：帥將各1、仕士各2、相象各2、俥車各2、傌馬各2、炮包各2、兵卒各5
        int[] counts = {1, 2, 2, 2, 2, 2, 5};
        for (int side = 0; side < 2; side++) {
            String[] names = (side == 0) ? RED_NAMES : BLACK_NAMES;
            for (int type = 0; type < 7; type++) {
                for (int n = 0; n < counts[type]; n++) {
                    Chess c = new Chess(names[type], WEIGHTS[type], side, -1, -1);
                    allChess.add(c);
                }
            }
        }
        Collections.shuffle(allChess, random);

        List<int[]> positions = new ArrayList<>();
        for (int r = 0; r < ROWS; r++) {
            for (int c = 0; c < COLS; c++) {
                positions.add(new int[]{r, c});
            }
        }
        Collections.shuffle(positions, random);

        for (int i = 0; i < 32 && i < positions.size(); i++) {
            int[] pos = positions.get(i);
            Chess c = allChess.get(i);
            c.setPosition(pos[0], pos[1]);
            board[pos[0]][pos[1]] = c;
        }
    }

    /** 印出所有棋子的名字、位置 */
    public void showAllChess() {
        System.out.println("\n  1   2   3   4   5   6   7   8");
        for (int r = 0; r < ROWS; r++) {
            char rowLabel = (char) ('A' + r);
            System.out.print(rowLabel + " ");
            for (int c = 0; c < COLS; c++) {
                Chess ch = board[r][c];
                String s = (ch == null) ? "＿" : ch.toDisplayString();
                System.out.print(s + "  ");
            }
            System.out.println();
        }
    }

    @Override
    public void setPlayers(Player player1, Player player2) {
        this.player1 = player1;
        this.player2 = player2;
    }

    @Override
    public boolean gameOver() {
        if (!gameStarted) return false;
        int redCount = 0, blackCount = 0;
        for (Chess c : allChess) {
            if (c.getSide() == 0) redCount++;
            else blackCount++;
        }
        return redCount == 0 || blackCount == 0;
    }

    @Override
    public boolean move(int location) {
        int toCol = location % 10;
        int toRow = (location / 10) % 10;
        int fromCol = (location / 100) % 10;
        int fromRow = location / 1000;
        return moveInternal(fromRow, fromCol, toRow, toCol);
    }

    /** 內部使用的移動方法 */
    public boolean move(int fromRow, int fromCol, int toRow, int toCol) {
        return moveInternal(fromRow, fromCol, toRow, toCol);
    }

    private boolean moveInternal(int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow < 0 || fromRow >= ROWS || fromCol < 0 || fromCol >= COLS) return false;
        if (toRow < 0 || toRow >= ROWS || toCol < 0 || toCol >= COLS) return false;
        Chess from = board[fromRow][fromCol];
        if (from == null || !from.isRevealed()) return false;
        int mySide = (currentPlayer == 0) ? player1.getSide() : player2.getSide();
        if (from.getSide() != mySide) return false;

        Chess to = board[toRow][toCol];
        if (from.isCannon()) {
            return cannonMove(from, fromRow, fromCol, toRow, toCol, to);
        }
        // 一般棋子：只能走相鄰格
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        if (dr + dc != 1) return false;
        if (to == null) return doMove(from, fromRow, fromCol, toRow, toCol);
        if (to.getSide() == from.getSide()) return false;
        if (!canEat(from, to)) return false;
        return doCapture(from, fromRow, fromCol, toRow, toCol, to);
    }

    /** 砲移動：必須跳過恰好一個棋子（炮台）到目標格，目標格必須是敵子 */
    private boolean cannonMove(Chess cannon, int fr, int fc, int tr, int tc, Chess target) {
        if (target == null) return false;  // 砲不能走空格，只能吃子
        if (target.getSide() == cannon.getSide()) return false;
        int dr = tr - fr, dc = tc - fc;
        if (dr != 0 && dc != 0) return false;  // 只能直線
        int stepR = Integer.compare(dr, 0);
        int stepC = Integer.compare(dc, 0);
        int count = 0;
        int r = fr + stepR, c = fc + stepC;
        while (r != tr || c != tc) {
            if (r < 0 || r >= ROWS || c < 0 || c >= COLS) return false;
            if (board[r][c] != null) count++;
            r += stepR;
            c += stepC;
        }
        return count == 1;  // 恰好一個炮台
    }

    /** 一般棋子能否吃目標（台灣暗棋規則） */
    private boolean canEat(Chess attacker, Chess target) {
        int aw = attacker.getWeight(), tw = target.getWeight();
        if (aw == 7) {  // 將/帥：不可吃兵卒
            return tw != 1;
        }
        if (aw == 1) {  // 兵/卒：可吃將帥，不可吃砲
            return tw == 7;
        }
        if (aw == 2) return false;  // 砲不在此處理
        return tw <= aw;  // 可吃同等或較低
    }

    private boolean doMove(Chess piece, int fr, int fc, int tr, int tc) {
        board[fr][fc] = null;
        piece.setPosition(tr, tc);
        board[tr][tc] = piece;
        switchTurn();
        return true;
    }

    private boolean doCapture(Chess attacker, int fr, int fc, int tr, int tc, Chess target) {
        allChess.remove(target);
        board[fr][fc] = null;
        attacker.setPosition(tr, tc);
        board[tr][tc] = attacker;
        switchTurn();
        return true;
    }

    private void switchTurn() {
        currentPlayer = 1 - currentPlayer;
        selectedRow = null;
        selectedCol = null;
    }

    /** 翻開指定位置的棋子 */
    public boolean flip(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return false;
        Chess c = board[row][col];
        if (c == null || c.isRevealed()) return false;
        c.setRevealed(true);
        if (!gameStarted) {
            gameStarted = true;
            player1.setSide(c.getSide());
            player2.setSide(1 - c.getSide());
            currentPlayer = (c.getSide() == player1.getSide()) ? 0 : 1;
        }
        switchTurn();  // 翻棋後換對方回合
        return true;
    }

    /** 解析輸入如 A2, B3 為 (row, col) */
    public static int[] parsePosition(String input) {
        if (input == null || input.length() < 2) return null;
        input = input.trim().toUpperCase();
        char rowCh = input.charAt(0);
        if (rowCh < 'A' || rowCh > 'D') return null;
        int row = rowCh - 'A';
        try {
            int col = Integer.parseInt(input.substring(1).trim());
            if (col < 1 || col > 8) return null;
            return new int[]{row, col - 1};
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /** 執行一輪遊戲（Console 介面） */
    public void run() {
        generateChess();
        setPlayers(new Player("玩家1"), new Player("玩家2"));
        System.out.println("=== 台灣暗棋 ===");
        System.out.println("輸入格式：A1, B2 等（列 A-D，行 1-8）");
        System.out.println("未翻開的棋子輸入位置可翻開；已翻開的己方棋子輸入後再輸入目的地可移動或吃子。");
        while (!gameOver()) {
            showAllChess();
            Player cur = (currentPlayer == 0) ? player1 : player2;
            String sideName = (cur.getSide() == 0) ? "紅方" : (cur.getSide() == 1) ? "黑方" : "（請翻棋決定陣營）";
            System.out.println("\n輪到 " + cur.getName() + " " + sideName);

            if (selectedRow != null && selectedCol != null) {
                System.out.print("請輸入目的地 (例如 B3)：");
            } else {
                System.out.print("請輸入位置 (翻棋或選擇棋子)：");
            }

            String line = scanner.nextLine().trim();
            if (line.isEmpty()) continue;
            int[] pos = parsePosition(line);
            if (pos == null) {
                System.out.println("輸入格式錯誤，請輸入如 A2, B3");
                continue;
            }
            int r = pos[0], c = pos[1];
            Chess piece = board[r][c];

            if (selectedRow != null && selectedCol != null) {
                if (r == selectedRow && c == selectedCol) {
                    System.out.println("已取消選擇。");
                    selectedRow = null;
                    selectedCol = null;
                    continue;
                }
                boolean ok = move(selectedRow, selectedCol, r, c);
                if (!ok) {
                    System.out.println("移動或吃子失敗，請重試。");
                    selectedRow = null;
                    selectedCol = null;
                }
                continue;
            }

            if (piece == null) {
                System.out.println("該位置無棋子。");
                continue;
            }
            if (!piece.isRevealed()) {
                if (!gameStarted || (gameStarted && (player1.getSide() == -1 || currentPlayer == 0 || currentPlayer == 1))) {
                    flip(r, c);
                    System.out.println("翻開：" + piece.getName());
                }
                continue;
            }
            int mySide = cur.getSide();
            if (piece.getSide() != mySide) {
                System.out.println("該棋子為敵方或尚未輪到您，請選擇己方已翻開的棋子。");
                continue;
            }
            selectedRow = r;
            selectedCol = c;
            System.out.println("已選擇 " + piece.getName() + "，請輸入目的地。");
        }
        showAllChess();
        int winner = (player1.getSide() == 0) ? (hasPieces(0) ? 0 : 1) : (hasPieces(1) ? 1 : 0);
        System.out.println("\n遊戲結束！" + ((winner == 0) ? player1 : player2).getName() + " 獲勝！");
    }

    private boolean hasPieces(int side) {
        for (Chess c : allChess) {
            if (c.isRevealed() && c.getSide() == side) return true;
        }
        return false;
    }
}
