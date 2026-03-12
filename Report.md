# H1 Report

* Name: 王建葦
* ID: D1210799

---

## 題目：練習 2.4.3 象棋翻棋遊戲

考慮一個象棋翻棋遊戲，32 個棋子會隨機的落在 4×8 的棋盤上。透過 Chess 的建構子產生這些棋子並隨機編排位置，再印出這些棋子的名字、位置。

- ChessGame 繼承 AbstractGame
- AbstractGame 宣告抽象方法：setPlayers、gameOver、move
- 簡單版 Console 介面，使用者輸入位置 (如 A2, B3) 翻棋或移動/吃子

## 設計方法概述

1. **AbstractGame**：抽象類別，定義遊戲介面（setPlayers、gameOver、move）
2. **Player**：玩家類別，儲存名稱與陣營
3. **Chess**：棋子類別，包含 name、weight、side、位置，實作 toString 顯示
4. **ChessGame**：繼承 AbstractGame，實作 generateChess、showAllChess、遊戲邏輯
5. **台灣暗棋規則**：將>士>象>車>馬>包>兵，將不可吃兵、兵可吃將，包/砲需隔子吃

## 程式、執行畫面及其說明

### 類別架構

```
AbstractGame (抽象)
    └── ChessGame
Chess, Player
```

### 主要程式碼

**Chess 建構子與 toString：**

```java
public Chess(String name, int weight, int side, int row, int col) {
    this.name = name;
    this.weight = weight;
    this.side = side;
    this.row = row;
    this.col = col;
    this.flipped = false;
}
@Override
public String toString() {
    return flipped ? name : "Ｘ";
}
```

**ChessGame.generateChess()：**

```java
public void generateChess() {
    // 建立紅黑各 16 子，shuffle 後擺放於 4×8 棋盤
    Collections.shuffle(allChess, new Random());
    for (int i = 0; i < TOTAL; i++) {
        int r = i / COLS, c = i % COLS;
        Chess ch = allChess.get(i);
        ch.setLocation(r, c);
        board[r][c] = ch;
    }
}
```

### 執行畫面

棋盤顯示範例：

```
    1   2   3   4   5   6   7   8
A  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  
B  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  
C  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  
D  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  Ｘ  
```

翻棋後範例：

![](img/chess_screenshot.png)

*（請將實際執行畫面截圖放置於 `img/chess_screenshot.png`）*

## AI 使用狀況與心得

- **使用層級**：（請填寫 1–4）
- **概述**：（請描述與 AI 互動的次數與內容）
- **手動完成部分**：（請說明）
- **心得**：（請反思 AI 的實用性、限制、對學習的影響）
