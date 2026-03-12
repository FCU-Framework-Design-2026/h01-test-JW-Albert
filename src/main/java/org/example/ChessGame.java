package org.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

/**
 * 象棋翻棋遊戲 - 台灣暗棋
 * 規則：將(7)>士(6)>象(5)>車(4)>馬(3)>包(2)>兵(1)
 * 將/帥不可吃兵/卒，兵/卒可吃將/帥
 * 包/砲需隔一子（炮台）才能吃子
 */
public class ChessGame extends AbstractGame {
    private static final int ROWS = 4;
    private static final int COLS = 8;
    private static final int TOTAL = 32;

    // 台灣暗棋棋子：紅方與黑方各16子
    private static final String[] RED_NAMES = {"帥", "仕", "相", "俥", "傌", "砲", "兵"};
    private static final String[] BLACK_NAMES = {"將", "士", "象", "車", "馬", "包", "卒"};
    private static final int[] WEIGHTS = {7, 6, 5, 4, 3, 2, 1};  // 對應上述順序
    private static final int[] COUNTS = {1, 2, 2, 2, 2, 2, 5};  // 各棋子數量

    private Chess[][] board;
    private List<Chess> allChess;
    private int currentPlayer;  // 0 或 1
    private Integer selectedFrom;  // 已選中的棋子位置 (row*8+col)，null 表示尚未選擇
    private int firstFlipperSide = -1;  // 第一個翻開的棋子所屬方，決定雙方陣營

    public ChessGame() {
        this.board = new Chess[ROWS][COLS];
        this.allChess = new ArrayList<>();
        this.currentPlayer = 0;
        this.selectedFrom = null;
    }

    @Override
    public void setPlayers(Player p1, Player p2) {
        this.player1 = p1;
        this.player2 = p2;
    }

    /**
     * 產生 32 個棋子並隨機擺放
     */
    public void generateChess() {
        allChess.clear();
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                board[i][j] = null;
            }
        }

        int idx = 0;
        for (int t = 0; t < 2; t++) {
            int side = t;
            String[] names = (side == 0) ? RED_NAMES : BLACK_NAMES;
            for (int n = 0; n < names.length; n++) {
                for (int c = 0; c < COUNTS[n]; c++) {
                    allChess.add(new Chess(names[n], WEIGHTS[n], side, -1, -1));
                }
            }
        }

        Collections.shuffle(allChess, new Random());

        for (int i = 0; i < TOTAL; i++) {
            int r = i / COLS;
            int c = i % COLS;
            Chess ch = allChess.get(i);
            ch.setLocation(r, c);
            board[r][c] = ch;
        }
    }

    /**
     * 印出所有棋子的名字與位置（練習 2.4.3 要求）
     */
    public void printAllChessInfo() {
        String[] rowLabels = {"A", "B", "C", "D"};
        for (Chess ch : allChess) {
            int r = ch.getRow(), c = ch.getCol();
            String pos = rowLabels[r] + (c + 1);
            System.out.println(ch.getName() + " @ " + pos + " " + (ch.isFlipped() ? "(已翻)" : "(未翻)"));
        }
    }

    /**
     * 顯示棋盤狀態
     */
    public void showAllChess() {
        System.out.println("\n    1   2   3   4   5   6   7   8");
        String[] rowLabels = {"A", "B", "C", "D"};
        for (int i = 0; i < ROWS; i++) {
            System.out.print(rowLabels[i] + "  ");
            for (int j = 0; j < COLS; j++) {
                Chess ch = board[i][j];
                String s = (ch == null) ? "＿" : ch.toString();
                System.out.print(s + "  ");
            }
            System.out.println();
        }
        System.out.println();
    }

    @Override
    public boolean gameOver() {
        if (firstFlipperSide < 0) return false;  // 尚未開始
        int redCount = 0, blackCount = 0;
        for (Chess ch : allChess) {
            if (ch.getSide() == 0) redCount++;
            else blackCount++;
        }
        return redCount == 0 || blackCount == 0;
    }

    /**
     * 執行移動或翻棋
     * location 編碼：翻棋時 = pos (0-31)
     * 移動時 = from * 64 + to (from, to 各 0-31)
     */
    @Override
    public boolean move(int location) {
        if (location < 64) {
            return flipChess(location);
        }
        int from = location / 64;
        int to = location % 64;
        return moveOrCapture(from, to);
    }

    /**
     * 翻開指定位置的棋子
     */
    public boolean flipChess(int location) {
        int row = location / COLS;
        int col = location % COLS;
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return false;

        Chess ch = board[row][col];
        if (ch == null) return false;
        if (ch.isFlipped()) return false;

        ch.setFlipped(true);
        if (firstFlipperSide < 0) {
            firstFlipperSide = ch.getSide();
        }
        return true;
    }

    /**
     * 移動或吃子
     */
    public boolean moveOrCapture(int fromLoc, int toLoc) {
        int fromRow = fromLoc / COLS;
        int fromCol = fromLoc % COLS;
        int toRow = toLoc / COLS;
        int toCol = toLoc % COLS;

        Chess fromChess = board[fromRow][fromCol];
        Chess toChess = board[toRow][toCol];

        if (fromChess == null || !fromChess.isFlipped()) return false;
        if (fromChess.getSide() != getCurrentPlayerSide()) return false;

        boolean isCannon = fromChess.getName().equals("砲") || fromChess.getName().equals("包");

        if (isCannon) {
            return tryCannonMove(fromChess, toChess, fromRow, fromCol, toRow, toCol);
        }

        // 一般棋子：檢查是否為鄰格（上下左右）
        int dr = Math.abs(toRow - fromRow);
        int dc = Math.abs(toCol - fromCol);
        boolean isAdjacent = (dr == 1 && dc == 0) || (dr == 0 && dc == 1);
        if (!isAdjacent) return false;

        if (toChess == null) {
            // 移動到空格
            board[toRow][toCol] = fromChess;
            board[fromRow][fromCol] = null;
            fromChess.setLocation(toRow, toCol);
            return true;
        }

        if (!toChess.isFlipped()) {
            return captureHidden(fromChess, toChess, fromRow, fromCol, toRow, toCol);
        }
        return captureVisible(fromChess, toChess, fromRow, fromCol, toRow, toCol);
    }

    private boolean captureHidden(Chess fromChess, Chess toChess, int fromRow, int fromCol, int toRow, int toCol) {
        toChess.setFlipped(true);
        if (toChess.getSide() == fromChess.getSide()) {
            // 翻開是己方，不能吃，視為單純翻棋
            return true;
        }
        // 敵方暗棋：比大小
        if (canEat(fromChess, toChess)) {
            board[fromRow][fromCol] = null;
            board[toRow][toCol] = fromChess;
            fromChess.setLocation(toRow, toCol);
            allChess.remove(toChess);
            return true;
        } else {
            // 己方被吃
            board[fromRow][fromCol] = null;
            board[toRow][toCol] = toChess;
            allChess.remove(fromChess);
            return true;
        }
    }

    private boolean captureVisible(Chess fromChess, Chess toChess, int fromRow, int fromCol, int toRow, int toCol) {
        if (toChess.getSide() == fromChess.getSide()) return false;
        if (!canEat(fromChess, toChess)) return false;

        board[fromRow][fromCol] = null;
        board[toRow][toCol] = fromChess;
        fromChess.setLocation(toRow, toCol);
        allChess.remove(toChess);
        return true;
    }

    /**
     * 包/砲隔子吃：需隔一子（炮台）才能吃，不受階級限制
     */
    private boolean tryCannonMove(Chess fromChess, Chess toChess, int fromRow, int fromCol, int toRow, int toCol) {
        if (fromRow != toRow && fromCol != toCol) return false;  // 必須直線
        int count = countPiecesBetween(fromRow, fromCol, toRow, toCol);
        if (toChess == null) {
            if (count == 0) {
                board[toRow][toCol] = fromChess;
                board[fromRow][fromCol] = null;
                fromChess.setLocation(toRow, toCol);
                return true;
            }
            return false;
        }
        if (count != 1) return false;  // 吃子時必須隔 exactly 1 子
        if (!toChess.isFlipped()) {
            toChess.setFlipped(true);
            if (toChess.getSide() == fromChess.getSide()) return true;  // 己方，不能吃
            board[fromRow][fromCol] = null;
            board[toRow][toCol] = fromChess;
            fromChess.setLocation(toRow, toCol);
            allChess.remove(toChess);
            return true;
        }
        if (toChess.getSide() == fromChess.getSide()) return false;
        board[fromRow][fromCol] = null;
        board[toRow][toCol] = fromChess;
        fromChess.setLocation(toRow, toCol);
        allChess.remove(toChess);
        return true;
    }

    private int countPiecesBetween(int r1, int c1, int r2, int c2) {
        int count = 0;
        if (r1 == r2) {
            int min = Math.min(c1, c2), max = Math.max(c1, c2);
            for (int c = min + 1; c < max; c++) {
                if (board[r1][c] != null) count++;
            }
        } else {
            int min = Math.min(r1, r2), max = Math.max(r1, r2);
            for (int r = min + 1; r < max; r++) {
                if (board[r][c1] != null) count++;
            }
        }
        return count;
    }

    /**
     * 判斷 from 能否吃 to（台灣暗棋規則，不含包/砲）
     */
    private boolean canEat(Chess from, Chess to) {
        if (from.getWeight() == 7 && to.getWeight() == 1) return false;  // 將不能吃兵
        if (from.getWeight() == 1 && to.getWeight() == 7) return true;   // 兵可吃將
        return from.getWeight() >= to.getWeight();
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public void nextPlayer() {
        currentPlayer = 1 - currentPlayer;
    }

    public int getCurrentPlayerSide() {
        if (firstFlipperSide < 0) return 0;
        return (currentPlayer == 0) ? firstFlipperSide : (1 - firstFlipperSide);
    }

    public Integer getSelectedFrom() {
        return selectedFrom;
    }

    public void setSelectedFrom(Integer loc) {
        this.selectedFrom = loc;
    }

    public Chess getChessAt(int row, int col) {
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return null;
        return board[row][col];
    }

    public Chess getChessAt(int location) {
        int row = location / COLS;
        int col = location % COLS;
        return getChessAt(row, col);
    }

    /**
     * 解析輸入位置 (如 A2, B3) 為 location (0-31)
     */
    public static int parseLocation(String input) {
        if (input == null || input.length() < 2) return -1;
        input = input.trim().toUpperCase();
        char rowCh = input.charAt(0);
        int col;
        try {
            col = Integer.parseInt(input.substring(1).trim()) - 1;
        } catch (NumberFormatException e) {
            return -1;
        }
        int row = rowCh - 'A';
        if (row < 0 || row >= ROWS || col < 0 || col >= COLS) return -1;
        return row * COLS + col;
    }

    /**
     * 主程式：Console 遊戲迴圈
     */
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        ChessGame game = new ChessGame();
        game.setPlayers(new Player("玩家1", 0), new Player("玩家2", 1));
        game.generateChess();

        System.out.println("=== 台灣暗棋 ===");
        System.out.println("輸入格式：A1 ~ D8 (例：A2, B3)");
        System.out.println("未翻開的棋子輸入位置可翻開；已翻開的棋子需再輸入目的位置進行移動或吃子。");

        while (!game.gameOver()) {
            game.showAllChess();
            int side = game.getCurrentPlayerSide();
            String sideName = (side == 0) ? "紅方" : "黑方";
            System.out.print("【" + sideName + "】請輸入位置: ");

            String input = sc.nextLine().trim();
            if (input.isEmpty()) continue;

            int loc = parseLocation(input);
            if (loc < 0) {
                System.out.println("錯誤：無效位置，請輸入 A1~D8");
                continue;
            }

            Chess ch = game.getChessAt(loc);
            if (ch == null) {
                System.out.println("錯誤：該位置無棋子");
                continue;
            }

            if (!ch.isFlipped()) {
                if (game.flipChess(loc)) {
                    game.showAllChess();
                    game.nextPlayer();
                } else {
                    System.out.println("錯誤：無法翻開");
                }
            } else {
                if (game.getSelectedFrom() != null) {
                    int fromLoc = game.getSelectedFrom();
                    if (game.moveOrCapture(fromLoc, loc)) {
                        game.nextPlayer();
                        game.setSelectedFrom(null);
                    } else {
                        System.out.println("錯誤：無法移動或吃子，請重試");
                    }
                } else {
                    if (ch.getSide() != game.getCurrentPlayerSide()) {
                        System.out.println("錯誤：該棋子為對方所有");
                        continue;
                    }
                    game.setSelectedFrom(loc);
                    System.out.print("已選擇 " + input + "，請輸入目的位置: ");
                }
            }
        }

        game.showAllChess();
        System.out.println("遊戲結束！");
        sc.close();
    }
}
