package core.hoppers.model;

import core.common.Observer;
import core.common.solver.Configuration;
import core.common.solver.Solver;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * The model that stores the internal logic of the Hoppers
 * puzzle game.
 *
 * @author Tereza Lang (@TORITZA)
 */
public class HoppersModel {
    /** the collection of observers of this model */
    private final List<Observer<HoppersModel, String>> observers = new LinkedList<>();

    /** the current configuration */
    private HoppersConfig currentConfig;
    /** a file's initial configuration */
    private HoppersConfig originalConfig;

    /** "blank slate" config for front-end puzzle creation */
    private HoppersConfig creationConfig;
    /** number of frogs on the player-created board */
    private int redFrogCount;
    private int greenFrogCount;

    /** the solver that stores the path to the puzzle's solution */
    private Solver sol = new Solver();

    /** has the puzzle been solved? */
    private boolean solved = false;
    private static final String IS_SOLVED = "Already solved!";

    /** is the user on their second selection? */
    private boolean selectTwo = false;
    private int selectedRow;
    private int selectedCol;

    /**
     * The view calls this to add itself as an observer.
     *
     * @param observer the view
     */
    public void addObserver(Observer<HoppersModel, String> observer) {
        this.observers.add(observer);
    }

    /**
     * The model's state has changed, so inform the view via
     * the update method.
     */
    private void alertObservers(String msg) {
        for (var observer : observers) {
            observer.update(this, msg);
        }
    }

    /**
     * Create a new Hoppers puzzle, storing the initial layout to reset back
     * to when prompted.
     *
     * @param filename the file whose contents are to be configured into a
     *                 Hoppers puzzle
     */
    public HoppersModel(String filename) throws IOException {
        currentConfig = new HoppersConfig(filename);
        originalConfig = new HoppersConfig(currentConfig);
    }

    /**
     * Configures the game board to look a step further to its solution.
     */
    public void hint() {
        String hintMsg;

        if (solved) {
            // User has already won the game! No need for next step
            hintMsg = IS_SOLVED;
        } else {
            try {
                Collection<Configuration> solverPath = sol.solve(currentConfig);
                // convert to LinkedList to access removeFirst method
                LinkedList<Configuration> solutionPath = new LinkedList<>(solverPath);

                // remove the current config from solution path
                solutionPath.removeFirst();
                // create copy to avoid unwanted reference type (e.g. the game board!) modification
                currentConfig = new HoppersConfig((HoppersConfig) solutionPath.removeFirst());
                hintMsg = "Next step!";
            } catch (NullPointerException e) {
                // If solution path is empty, there can be no solution from this point on
                hintMsg = "No solution!";
            }
        }

        solved = currentConfig.isSolution();

        alertObservers(hintMsg);
    }

    /**
     * Loads a puzzle representation of the file provided.
     *
     * @param filename the file whose contents are to be configured into a
     *                 Hoppers puzzle
     */
    public void load(String filename) {
        String loadMsg;

        try {
            currentConfig = new HoppersConfig(filename);
            originalConfig = new HoppersConfig(currentConfig);
            loadMsg = "Loaded: " + filename;
        } catch (IOException e) {
            loadMsg = "Failed to load: " + filename;
        }

        // Is the loaded file already a solution?
        solved = currentConfig.isSolution();

        alertObservers(loadMsg);
    }

    /**
     * Stores the user's first selection of some frog on the board, and then
     * verifies if it can jump to the space the user chooses upon their second
     * selection.
     *
     * @param r the row value of the space selected
     * @param c the column value of the space selected
     */
    public void select(String r, String c) {
        int row = Integer.parseInt(r);
        int col = Integer.parseInt(c);
        String selectMsg;

        // User is selecting a space on the board...
        if (solved) {
            // User has already won the game!
            // "Locks" the game board until a different command has been inputted
            selectMsg = IS_SOLVED;
        } else if (row < 0 || row >= currentConfig.getRow() ||
                col < 0 || col >= currentConfig.getCol()) {
            // User chose a cell that does not exist on the game board
            selectMsg = "Selection is off the board!";
        } else if (!currentConfig.getCell(row, col).equals(HoppersConfig.GREEN_FROG) &&
                !currentConfig.getCell(row, col).equals(HoppersConfig.RED_FROG) && !selectTwo) {
            // User chose either an EMPTY (.) or INVALID (*) cell
            selectMsg = "No frog at (" + r + ", " + c + ")";
        } else if (!selectTwo) {
            // User is on their first selection
            selectMsg = "Selected (" + r + ", " + c + ")";
            selectedRow = row;
            selectedCol = col;
            selectTwo = true;
        } else {
            // User is on their second selection
            if (currentConfig.validateDirection(selectedRow, selectedCol, row, col)) {
                // operation is possible within the rules of the game
                selectMsg = "Jumped from (" + selectedRow + ", " + selectedCol + ") " +
                        "to (" + row + ", " + col + ")";
                if (currentConfig.isSolution()) {
                    // User has won the game!
                    selectMsg = "You win!";
                    solved = true;
                }
                selectTwo = false;
            } else {
                // cannot perform user's intended move; breaks an established rule
                selectMsg = "Can't jump from (" + selectedRow + ", " + selectedCol + ") " +
                        "to (" + row + ", " + col + ")";
                selectTwo = false;
            }
        }

        alertObservers(selectMsg);
    }

    /**
     * Resets the game board back to the initial puzzle.
     */
    public void reset() {
        String resetMsg = "Puzzle reset!";
        currentConfig = new HoppersConfig(originalConfig);
        solved = currentConfig.isSolution();

        alertObservers(resetMsg);
    }

    /**
     * Create a new, blank board upon entering creation mode, priming it
     * for user-driven frog placement.
     */
    public void create(int row, int col) {
        String createMsg = "Creating new Hoppers puzzle...";
        creationConfig = new HoppersConfig(row, col);
        redFrogCount = 0;
        greenFrogCount = 0;

        alertObservers(createMsg);
    }

    /**
     * _____
     *
     * @param r
     * @param c
     */
    public void place(String r, String c, String frogType) {
        int maxRedFrog = 1;
        int maxGreenFrogs = creationConfig.getValidSpaces() - 1;

        String placeMsg;
        int row = Integer.parseInt(r);
        int col = Integer.parseInt(c);

        if (row < 0 || row >= creationConfig.getRow() ||
                col < 0 || col >= creationConfig.getCol()) {
            placeMsg = "Cannot build off the board";
        } else if (creationConfig.getCell(row, col).equals(HoppersConfig.INVALID_SPACE)) {
            placeMsg = "Invalid space for frog placement!";
        } else if (frogType.equals(HoppersConfig.RED_FROG) && redFrogCount == maxRedFrog) {
            placeMsg = "Exceeded Red Frog placement!";
        } else if (frogType.equals(HoppersConfig.GREEN_FROG) && greenFrogCount ==
                maxGreenFrogs) {
            placeMsg = "Exceeded Green Frog placement!";
        } else {
            creationConfig.changeCell(row, col, frogType);
            if (frogType.equals(HoppersConfig.RED_FROG)) {
                placeMsg = "Placed Red Frog at (" + row + ", " + col + ")";
            } else {
                placeMsg = "Placed Green Frog at ("+ row + ", " + col + ")";
            }
        }


            alertObservers(placeMsg);
        }


    /**
     * A getter for the HoppersConfig representing the model's current
     * puzzle.
     *
     * @return the model's HoppersConfig
     */
    public HoppersConfig getBoard() { return currentConfig; }

    /**
     * A getter for the HoppersConfig being manually created & designed by
     * the user.
     *
     * @return the model's creationConfig
     */
    public HoppersConfig getCreationConfig() { return creationConfig; }

    /**
     * The string representation of the Hoppers puzzle with some added
     * row/column index visuals:
     * (example)
     *         0 1 2 3 4
     *        ----------
     *      0| . * . * .
     *      1| * G * . *
     *      2| . * R * .
     *      3| * G * G *
     *      4| G * . * .
     *
     * @return the string in the format showcased above
     */
    @Override
    public String toString() {
        // create index header
        StringBuilder finalString = new StringBuilder("  ");
        for (int i = 0; i < currentConfig.getCol(); i++) {
            finalString.append(" ").append(i);
        }
        finalString.append("\n  ");
        finalString.append("-".repeat(currentConfig.getCol() * 2));

        // build side header & main Hoppers grid
        finalString.append("\n");
        String[] boardRows = currentConfig.toString().split("\n");
        for (int i = 0; i < currentConfig.getRow(); i++) {
            finalString.append(i).append("| ").append(boardRows[i])
                    .append("\n");
        }

        return finalString.toString();
    }

}

