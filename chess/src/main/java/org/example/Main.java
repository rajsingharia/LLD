package org.example;

import java.util.*;
import java.util.concurrent.*;

record Player(String name, boolean isBlack) {}

record Move(Player player, int startRow, int startCol, int endRow, int endCol){}

abstract class Piece {
    private final boolean isBlack;

    Piece(boolean isBlack) {
        this.isBlack = isBlack;
    }

    public boolean isBlack() { return isBlack; }

    public abstract boolean canMove(Move move, Board board);
}

class King extends Piece {
    King(boolean isBlack) { super(isBlack); }
    @Override
    public boolean canMove(Move move, Board board) {
        int dr = Math.abs(move.endRow() - move.startRow());
        int dc = Math.abs(move.endCol() - move.startCol());
        return dr <= 1 && dc <= 1;
    }
}

class Queen extends Piece {
    Queen(boolean isBlack) { super(isBlack); }
    @Override
    public boolean canMove(Move move, Board board) {
        int dr = Math.abs(move.endRow() - move.startRow());
        int dc = Math.abs(move.endCol() - move.startCol());
        return (dr == dc || dr == 0 || dc == 0);
    }
}

class Rook extends Piece {
    Rook(boolean isBlack) { super(isBlack); }
    @Override
    public boolean canMove(Move move, Board board) {
        return (move.startRow() == move.endRow() || move.startCol() == move.endCol());
    }
}

class Bishop extends Piece {
    Bishop(boolean isBlack) { super(isBlack); }
    @Override
    public boolean canMove(Move move, Board board) {
        return Math.abs(move.endRow() - move.startRow()) == Math.abs(move.endCol() - move.startCol());
    }
}

class Knight extends Piece {
    Knight(boolean isBlack) { super(isBlack); }
    @Override
    public boolean canMove(Move move, Board board) {
        int dr = Math.abs(move.endRow() - move.startRow());
        int dc = Math.abs(move.endCol() - move.startCol());
        return (dr == 2 && dc == 1) || (dr == 1 && dc == 2);
    }
}

class Pawn extends Piece {

    Pawn(boolean isBlack) {
        super(isBlack);
    }

    @Override
    public boolean canMove(Move move, Board board) {
        int dir = isBlack() ? 1 : -1;
        int dr = move.endRow() - move.startRow();
        int dc = Math.abs(move.endCol() - move.startCol());

        if(dc == 0 && dr == dir && board.getCell(move.endRow(), move.endCol()).getPiece() == null) {
            return true;
        }

        return dc == 1 && dr == dir && board.getCell(move.endRow(), move.endCol()).getPiece() != null;
    }
}

class Cell {
    private final int row, col;
    private Piece piece;

    Cell(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Piece getPiece() {
        return piece;
    }

    public void setPiece(Piece piece) {
        this.piece = piece;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }
}

class Board {
    private final Cell[][] board;
    private final int size;

    Board(int size) {
        this.size = size;
        this.board = new Cell[size][size];
        for(int i=0;i<size;i++) {
            for(int j=0;j<size;j++) {
                board[i][j] = new Cell(i,j);
            }
        }
        this.defaultResetBoard();
    }

    public Cell getCell(int row, int col) { return board[row][col]; }

    public void defaultResetBoard() {
        // Clear
        for(int i=0;i<size;i++) {
            for(int j=0;j<size;j++) {
                board[i][j].setPiece(null);
            }
        }

        // Pawns
        for(int j=0;j<8;j++) {
            board[1][j].setPiece(new Pawn(true));   // black pawns
            board[6][j].setPiece(new Pawn(false));  // white pawns
        }

        // Rooks
        board[0][0].setPiece(new Rook(true)); board[0][7].setPiece(new Rook(true));
        board[7][0].setPiece(new Rook(false)); board[7][7].setPiece(new Rook(false));

        // Knights
        board[0][1].setPiece(new Knight(true)); board[0][6].setPiece(new Knight(true));
        board[7][1].setPiece(new Knight(false)); board[7][6].setPiece(new Knight(false));

        // Bishops
        board[0][2].setPiece(new Bishop(true)); board[0][5].setPiece(new Bishop(true));
        board[7][2].setPiece(new Bishop(false)); board[7][5].setPiece(new Bishop(false));

        // Queens
        board[0][3].setPiece(new Queen(true));
        board[7][3].setPiece(new Queen(false));

        // Kings
        board[0][4].setPiece(new King(true));
        board[7][4].setPiece(new King(false));
    }

    public boolean movePiece(Move move) {
        Piece currentPiece = board[move.startRow()][move.startCol()].getPiece();
        if(currentPiece == null || currentPiece.isBlack() != move.player().isBlack()) return false;
        if(!currentPiece.canMove(move, this)) return false;

        Piece target = board[move.endRow()][move.endCol()].getPiece();
        if(target != null && target.isBlack() == currentPiece.isBlack()) return false;

        // Move piece
        board[move.endRow()][move.endCol()].setPiece(currentPiece);
        board[move.startRow()][move.startCol()].setPiece(null);
        return true;
    }

    public void printBoard() {
        for(int i=0;i<size;i++) {
            for(int j=0;j<size;j++) {
                Piece p = board[i][j].getPiece();
                if(p == null) System.out.print(". ");
                else System.out.print((p.isBlack()? "B":"W") + p.getClass().getSimpleName().charAt(0) + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
}

class PlayerMoveInput {
    public Move takeInput(Player player) {
        Scanner scanner = new Scanner(System.in);
        String playerColor = "Black";
        if(!player.isBlack()) {
            playerColor = "White";
        }
        System.out.println(player.name() + " (" + playerColor + ") - Enter move: startRow startCol endRow endCol :: ");
        int sr = scanner.nextInt();
        int sc = scanner.nextInt();
        int er = scanner.nextInt();
        int ec = scanner.nextInt();
        return new Move(player, sr, sc, er, ec);
    }
}

class Game implements Runnable {
    private final Board board;
    private final Player whitePlayer, blackPlayer;
    private boolean isBlackTurn;
    private final PlayerMoveInput playerMoveInputs;

    Game(int size, Player p1, Player p2, boolean isBlackTurn, PlayerMoveInput inputs) {
        this.board = new Board(size);
        this.playerMoveInputs = inputs;
        if(p1.isBlack() == p2.isBlack()) {
            throw new RuntimeException("Both players can't be same color!");
        }
        this.blackPlayer = p1.isBlack() ? p1 : p2;
        this.whitePlayer = p1.isBlack() ? p2 : p1;
        this.isBlackTurn = isBlackTurn;
    }

    @Override
    public void run() {
        while(true) {
            board.printBoard();
            Player currentPlayer = isBlackTurn ? blackPlayer : whitePlayer;
            Move move = playerMoveInputs.takeInput(currentPlayer);
            boolean moved = board.movePiece(move);

            if(!moved) {
                System.out.println("Invalid move! Try again.");
                continue;
            }

            // Check if opponent King is captured
            if(isKingCaptured(!currentPlayer.isBlack())) {
                board.printBoard();
                System.out.println(currentPlayer.name() + " wins!");
                break;
            }

            isBlackTurn = !isBlackTurn;
        }
    }

    private boolean isKingCaptured(boolean forBlack) {
        for(int i=0;i<8;i++) for(int j=0;j<8;j++) {
            Piece p = board.getCell(i,j).getPiece();
            if(p instanceof King && p.isBlack() == forBlack) return false;
        }
        return true;
    }

    public Player getWhitePlayer() { return whitePlayer; }
    public Player getBlackPlayer() { return blackPlayer; }
}

class GameExecutor {
    private final Map<String, ExecutorService> executorServices = new ConcurrentHashMap<>();

    public void startNewGame(Game game) {
        String key = this.generateKey(game);
        executorServices.put(key, Executors.newSingleThreadExecutor());
        executorServices.get(key).submit(game);
    }

    public void terminateGame(Game game) {
        String key = this.generateKey(game);
        executorServices.get(key).shutdownNow();
    }

    private String generateKey(Game game) {
        return game.getWhitePlayer().name() + "_" + game.getBlackPlayer().name();
    }
}

public class Main {
    public static void main(String[] args) {
        GameExecutor gameExecutor = new GameExecutor();
        PlayerMoveInput playerMoveInput = new PlayerMoveInput();

        Player player1 = new Player("Raj", false);
        Player player2 = new Player("Priyal", true);

        Game game = new Game(8, player1, player2, true, playerMoveInput);
        gameExecutor.startNewGame(game);

//        gameExecutor.terminateGame(game);
    }
}
