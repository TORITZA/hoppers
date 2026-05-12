package core.hoppers.model;

import core.common.solver.Configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * A configuration representing Hoppers, a classic peg solitaire jumping game. Frogs are populated
 * on a board. When a frog jumps over another frog, the frog that was jumped over is removed from
 * the board. The goal is to leave the red frog as the only frog on the game board!
 *
 * @author Tereza Lang (@TORITZA)
 */
public class HoppersConfig implements Configuration {

    /** static Strings representing the values on the Hoppers board */
    public static final String RED_FROG = "R";
    public static final String GREEN_FROG = "G";
    public static final String EMPTY_SPACE = ".";
    public static final String INVALID_SPACE = "*";

    /** amt of cell spaces that frogs can move */
    private static final int DIAGONAL = 2;
    private static final int CARDINAL = 4;

    /** the board and its dimensions */
    private int ROW;
    private int COL;

    /** the board itself */
    private String[][] board;

    /**
     *  The constructor for an initial HoppersConfig. It requires the name of a file,
     *  creating a Hoppers game representation from its contents.
     *
     * @param filename the name of the provided file
     * @throws IOException thrown when file cannot be found
     */
    public HoppersConfig(String filename) throws IOException {

        try (BufferedReader rd = new BufferedReader(new FileReader(filename))) {
            // read in and store the dimensions
            String line = rd.readLine();
            String[] fields;
            fields = line.split("\\s+");

            ROW = Integer.parseInt(fields[0]);
            COL = Integer.parseInt(fields[1]);
            board = new String[ROW][COL];

            // populate board with the file's values
            for (int l = 0; l < ROW; l++) {
                String[] rowToCopy = rd.readLine().split("\\s+");
                System.arraycopy(rowToCopy, 0, board[l], 0, rowToCopy.length);
            }
        }
    }

    /**
     * A constructor for the purpose of creating a board emptied of frogs so
     * that the user can make their own Hoppers puzzle, populating it with
     * frogs in places of their choosing.
     *
     * @param row amount of rows in the empty board
     * @param col amount of columns in the empty board
     */
    public HoppersConfig(int row, int col) {
        ROW = row;
        COL = col;
        board = new String[ROW][COL];

        for (int r = 0; r < ROW; r++) {
            for (int c = 0; c < COL; c++) {
                // if current row is even...
                if (r % 2 == 0) {
                    if (c % 2 == 0) {
                        board[r][c] = EMPTY_SPACE;
                    } else {
                        board[r][c] = INVALID_SPACE;
                    }
                } else { // if current row is odd...
                    if (c % 2 == 0) {
                        board[r][c] = INVALID_SPACE;
                    } else {
                        board[r][c] = EMPTY_SPACE;
                    }
                }

            }
        }

    }


    /**
     * A deep copy constructor designed to keep the game boards of each successor
     * configuration isolated, preventing any unintended modification.
     * The dimensions of the board remain the same, while the 2D array itself is
     * deeply copied.
     *
     * @param orig the original HoppersConfig that is to be deep-copied
     */
    public HoppersConfig(HoppersConfig orig) {
        this.ROW = orig.ROW;
        this.COL = orig.COL;

        this.board = new String[ROW][COL];
        for (int i = 0; i < ROW; i++) {
            System.arraycopy(orig.board[i], 0, this.board[i], 0, COL);
        }

    }

    /**
     *  Is the configuration the solution to the initial Hoppers puzzle?
     *  Identified if the board no longer contains any green frogs, and the
     *  red frog is the last peg standing.
     *
     * @return true if the HoppersConfig qualifies as a solution; otherwise,
     * returns false
     */
    @Override
    public boolean isSolution() {
        boolean redLeft = false;

        for (int r = 0; r < ROW; r++) {
            for (int c = 0; c < COL; c++) {
                if (board[r][c].equals(GREEN_FROG)) {
                    return false;
                } else if (board[r][c].equals(RED_FROG)) {
                    redLeft = true;
                }
            }
        }

        return redLeft;
    }

    /**
     * Modifies the game board to simulate the move of a frog jump.
     *
     * @param fromRow the row the jumping frog was originally on
     * @param fromCol the column the jumping frog was originally on
     * @param toRow the row the frog will land on
     * @param toCol the column the frog will land on
     */
    public void frogJump(int fromRow, int fromCol, int toRow, int toCol) {

        board[toRow][toCol] = switch (board[fromRow][fromCol]) {
            case GREEN_FROG -> GREEN_FROG;
            case RED_FROG -> RED_FROG;
            default -> throw new IllegalStateException("Unexpected value " + board[fromRow][fromCol] +
                    " at " + fromRow + ", " + fromCol);
        };

        board[fromRow][fromCol] = EMPTY_SPACE;

        // midpoint formula
        int midRow = (fromRow + toRow) / 2;
        int midCol = (fromCol + toCol) / 2;
        board[midRow][midCol] = EMPTY_SPACE;
    }

    /**
     * Generates a list of successor based on each possible move for every frog on
     * the board, considering the parity of the grid space they're settled on.
     *
     * @return a list of this config's successors
     */
    @Override
    public Collection<Configuration> getNeighbors() {

        List<Configuration> neighbors = new LinkedList<>();

        for (int r = 0; r < ROW; r++) {
            for (int c = 0; c < COL; c++) {
                int landingSpotRow;
                int landingSpotCol;
                int midRow;
                int midCol;
                if (board[r][c].equals(RED_FROG) || board[r][c].equals(GREEN_FROG)) {
                    // DIRECTION CHECKING:
                    int[][] diagonals = {
                            {r - DIAGONAL, c - DIAGONAL}, // northwest
                            {r + DIAGONAL, c + DIAGONAL}, // southeast
                            {r - DIAGONAL, c + DIAGONAL}, // northeast
                            {r + DIAGONAL, c - DIAGONAL} // southwest
                    };
                    for (int[] direction : diagonals) {
                        landingSpotRow = direction[0];
                        landingSpotCol = direction[1];
                        midRow = (landingSpotRow + r) / 2;
                        midCol = (landingSpotCol + c) / 2;
                        validateDirection(neighbors, r, c, landingSpotRow, landingSpotCol, midRow, midCol);
                    }
                    // If the board space is an even cell, also check the CARDINAL directions:
                    if (r % 2 == 0 && c % 2 == 0) {
                        int[] cardinals = {
                                r - CARDINAL, // north
                                c + CARDINAL, // east
                                r + CARDINAL, // south
                                c - CARDINAL // west
                        };
                        for (int i = 0; i < cardinals.length; i++) {
                            if (i % 2 == 0) {
                                landingSpotRow = cardinals[i];
                                midRow = (landingSpotRow + r) / 2;
                                landingSpotCol = c;
                                midCol = c;
                            } else {
                                landingSpotRow = r;
                                midRow = r;
                                landingSpotCol = cardinals[i];
                                midCol = (landingSpotCol + c) / 2;
                            }

                            validateDirection(neighbors, r, c, landingSpotRow, landingSpotCol,
                                    midRow, midCol);
                        }


                    }
                }

            }
        }
        return neighbors;
    }

    /**
     * A getter for the number of rows in this configuration's game board.
     *
     * @return this config's ROW value
     */
    public int getRow() { return this.ROW; }

    /**
     * A getter for the number of columns in this configuration's game board.
     *
     * @return this config's COL value
     */
    public int getCol() { return this.COL; }

    /**
     * A getter for the contents of a specified space on this configuration's
     * game board.
     *
     * @param r the row of the desired cell
     * @param c the column of the desired cell
     * @return the character at the specified space
     */
    public String getCell(int r, int c) { return board[r][c]; }

    /**
     * A setter for changing the contents of a specified space on this
     * configuration's game board.
     *
     * @param r the row of the desired cell to be changed
     * @param c the column of the desired cell to be changed
     * @param content the character to replace the one at the specified
     *                space
     */
    public void changeCell(int r, int c, String content) {
        board[r][c] = content;
    }

    /**
     * A helper function that validates a potential move on the game board. If the
     * intended operation passes all constraints, then the configuration is successfully
     * added to the parent configuration's list of neighbors.
     *
     * @param neighbors this configuration's list of neighbors
     * @param r the row value the frog is currently on
     * @param c the column value the frog is currently on
     * @param landingSpotRow the row number of the space the jumping frog wants to land on
     * @param landingSpotCol the column number of the space the jumping frog wants to
     *        land on
     * @param midRow the row value of the space in-between the two
     * @param midCol the column value of the space in-between the two
     */
    private void validateDirection(List<Configuration> neighbors, int r, int c, int landingSpotRow,
                                   int landingSpotCol, int midRow, int midCol) {
        // is the landing spot off the grid?
        if (landingSpotRow >= 0 && landingSpotRow < ROW && landingSpotCol >= 0
                && landingSpotCol < COL) {
            // is the landing spot empty?
            if (board[landingSpotRow][landingSpotCol].equals(EMPTY_SPACE)) {
                // is there a Green frog at the midpoint of the current frog's jump?
                if (board[midRow][midCol].equals(GREEN_FROG)) {
                    HoppersConfig move = new HoppersConfig(this);
                    move.frogJump(r, c, landingSpotRow, landingSpotCol);
                    neighbors.add(move);
                }
            }
        }
    }

    /**
     * The configuration's neighbor generation helper method refactored for HoppersModel
     * in order to validate player-controlled movement .
     *
     * @param r the row value the frog is currently on
     * @param c the column value the frog is currently on
     * @param landingSpotRow the row number of the space the jumping frog wants to
     *        land on
     * @param landingSpotCol the column number of the space the jumping frog wants to
     *        land on
     * @return true if the jump doesn't violate the rules of the game; otherwise,
     *         returns false
     */
    public boolean validateDirection(int r, int c, int landingSpotRow, int landingSpotCol) {
        // the possible diagonal spaces for the frog's current space
        int[][] diagonals = {
                {r - DIAGONAL, c - DIAGONAL}, // northwest
                {r + DIAGONAL, c + DIAGONAL}, // southeast
                {r - DIAGONAL, c + DIAGONAL}, // northeast
                {r + DIAGONAL, c - DIAGONAL} // southwest
        };
        // the possible cardinal spaces for this frog's current, even-numbered space
        int[] cardinals = {
                r - CARDINAL, // north
                c + CARDINAL, // east
                r + CARDINAL, // south
                c - CARDINAL // west
        };
        boolean isValidJump = false;
        int validRow;
        int validCol;
        int midRow = (r + landingSpotRow) / 2;
        int midCol = (c + landingSpotCol) / 2;

        for (int[] direction : diagonals) {
            validRow = direction[0];
            validCol = direction[1];
            if (validRow == landingSpotRow && validCol == landingSpotCol) {
                isValidJump = true;
            }
        }
        // If the board space is an even cell, also check the CARDINAL directions:
        if (r % 2 == 0 && c % 2 == 0) {
            for (int i = 0; i < cardinals.length; i++) {
                if (i % 2 == 0) {
                    validRow = cardinals[i];
                    if (validRow == landingSpotRow && c == landingSpotCol) {
                        isValidJump = true;
                    }
                } else {
                    validCol = cardinals[i];
                    if (validCol == landingSpotCol && r == landingSpotRow) {
                        isValidJump = true;
                    }
                }
            }
        }

        if (isValidJump) {
            // (PTUI) is the destination within the board's bounds?
            if (landingSpotRow >= 0 && landingSpotRow < ROW && landingSpotCol >= 0
                    && landingSpotCol < COL) {
                // is the landing spot empty?
                if (board[landingSpotRow][landingSpotCol].equals(EMPTY_SPACE)) {
                    // does the midpoint contain a frog to be captured?
                    if (board[midRow][midCol].equals(GREEN_FROG)) {
                        this.frogJump(r, c, landingSpotRow, landingSpotCol);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Two HoppersConfigs are equal to one another if their game boards are populated in
     * an identical fashion, and they share the same dimensions.
     *
     * @param other the reference object with which to compare.
     * @return true if the two objects are equal; otherwise, returns false
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof HoppersConfig otherConfig) {
            return Arrays.deepEquals(this.board, otherConfig.board) && this.ROW == otherConfig.ROW
                    && this.COL == otherConfig.COL;
        }
        return false;
    }

    /**
     * The hash code of a HoppersConfig is the sum of the game board's hash
     * code value and its dimensions.
     *
     * @return HoppersConfig hash code
     */
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board) + this.ROW + this.COL;
    }

    /**
     * Converts the HoppersConfig's game board into its string representation:
     *  (example)
     *              . * . * .
     *              * G * . *
     *              . * R * .
     *              * G * G *
     *
     * @return a string in the format described above
     */
    @Override
    public String toString() {
        String boardString = "";

        for (int i = 0; i < ROW; i++) {
            for (String el : board[i]) {
                boardString += el + " ";
            }
            if (i != ROW - 1) {
                boardString += "\n";
            }
        }

        return boardString;
    }
}

