package core.hoppers.model;

import core.common.Observer;
import core.common.solver.Configuration;
import core.common.solver.Solver;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
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
    /**
     * defines storage path for customCounter in user's home folder
     */
    private static final Path CUSTOM_COUNTER = Paths.get(System.getProperty("user.home"),
            ".custom_counter");

    /**
     * the collection of observers of this model
     */
    private final List<Observer<HoppersModel, String>> observers = new LinkedList<>();

    /**
     * the current configuration
     */
    private HoppersConfig currentConfig;
    /**
     * a file's initial configuration
     */
    private HoppersConfig originalConfig;

    /**
     * "blank slate" config for front-end puzzle creation
     */
    private HoppersConfig creationConfig;
    /**
     * number of frogs on the player-created board
     */
    private int redFrogCount;
    private int greenFrogCount;
    /**
     * max number of frogs on the player-created board
     */
    private int maxRedFrog;
    private int maxGreenFrogs;
    /**
     * the number of custom puzzles the user has already created
     */
    private static int customCount;
    /**
     * Is the model currently in creation mode?
     */
    private boolean creationMode = false;

    /**
     * the solver that stores the path to the puzzle's solution
     */
    private Solver sol = new Solver();

    /**
     * has the puzzle been solved?
     */
    private boolean solved = false;
    private static final String IS_SOLVED = "Already solved!";

    /**
     * is the user on their second selection?
     */
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


    // ---------------------------CREATION MODE-------------------------------

    /**
     * Create a new, blank board upon entering creation mode, priming it
     * for user-driven frog placement.
     */
    public void create(int row, int col) {
        String createMsg = "Creating new Hoppers puzzle...";
        creationConfig = new HoppersConfig(row, col);
        redFrogCount = 0;
        greenFrogCount = 0;
        maxRedFrog = 1;
        maxGreenFrogs = creationConfig.getValidSpaces() - 1;
        customCount = loadCount();

        alertObservers(createMsg);
    }

    /**
     * In creation mode, place a frog at a valid space selected by the
     * user.
     *
     * @param r the row value of the space selected
     * @param c the column value of the space selected
     */
    public void place(String r, String c, String frogType) {
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
                placeMsg = "Placed Green Frog at (" + row + ", " + col + ")";
            }
        }

        alertObservers(placeMsg);
    }

    /**
     * In creation mode, deletes a frog off the board, emptying the
     * space.
     *
     * @param r the row value of the space selected
     * @param c the column value of the space selected
     */
    public void delete(String r, String c) {
        String deleteMsg;
        int row = Integer.parseInt(r);
        int col = Integer.parseInt(c);

        if (creationConfig.getCell(row, col).equals(HoppersConfig.GREEN_FROG)) {
            creationConfig.changeCell(row, col, HoppersConfig.EMPTY_SPACE);
            greenFrogCount--;
            deleteMsg = "Green Frog deleted at (" + row + ", " + col + ")";
        } else if (creationConfig.getCell(row, col).equals(HoppersConfig.RED_FROG)) {
            creationConfig.changeCell(row, col, HoppersConfig.EMPTY_SPACE);
            redFrogCount--;
            deleteMsg = "Red Frog deleted at (" + row + "," + col + ")";
        } else {
            deleteMsg = "Nothing to delete here!";
        }

        alertObservers(deleteMsg);
    }

    /**
     * Upon Hoppers start-up, verify if the customCount exists. If it does,
     * read & store the number within the CUSTOM_COUNTER filer; if not, default to
     * count zero.
     *
     * @return the number of times the user has created a custom puzzle
     */
    public int loadCount() {
        try {
            if (Files.exists(CUSTOM_COUNTER)) {
                // read through all text content & parse first line as int
                String content = Files.readAllLines(CUSTOM_COUNTER).getFirst().trim();
                return Integer.parseInt(content);
            }
        } catch (IOException e) {
            System.err.println("Cannot read counter, defaulting to 0: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Each time the user makes & saves a puzzle in creation mode,
     * increment the customCount in their application memory, overwriting
     * the local CUSTOM_COUNTER file with the new value.
     *
     * @param newCount the updated integer, chronicling the total amount of puzzles
     *                 that the user has created & saved
     */
    public void saveCount(int newCount) {
        try {
            // converts the int to a string & overwrites the existing content in the file
            Files.writeString(CUSTOM_COUNTER, String.valueOf(newCount),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Failed to save counter! " + e.getMessage());
        }

    }

    /**
     * Saves a custom, user-created puzzle to a subdirectory (which is constructed
     * if it does not already exist) in the Hoppers data folder. Then, increments
     * the total number of such puzzles that the user has already saved.
     * Written into a file in the same format as the rest of the default Hoppers
     * puzzles:
     * (example)
     * [# of rows] [# of columns]
     * [content of HoppersConfig represented by its toString]
     * .
     * .
     * .
     */
    public void save() {
        String saveMsg;
        customCount++;
        saveCount(customCount);

        String subFolder = "custom";
        String fileName = "hopped-" + customCount + ".txt";
        String content = creationConfig.getRow() + " " + creationConfig.getCol();
        content = "\n" + creationConfig.toString();

        // get working root directory
        Path workingDir = Paths.get(".").toAbsolutePath().normalize();

        // target existing data folder
        File dataDir = new File(workingDir.toFile(), "data");

        // nest new subdirectory inside data folder
        File targetSubDir = new File(dataDir, subFolder);
        File targetFile = new File(targetSubDir, fileName);

        // safely create subdirectory if it does not already exist
        try {
            if (!targetSubDir.exists()) {
                targetSubDir.mkdirs();
            }
            // write custom puzzle to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(targetFile))) {
                writer.write(content);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        saveMsg = "Saved puzzle as " + fileName;
        alertObservers(saveMsg);
    }


    // -----------------GETTERS & toSTRING--------------------

    /**
     * A getter for the HoppersConfig representing the model's current
     * puzzle.
     *
     * @return the model's HoppersConfig
     */
    public HoppersConfig getBoard() {
        return currentConfig;
    }

    /**
     * A getter for the HoppersConfig being manually created & designed by
     * the user.
     *
     * @return the model's creationConfig
     */
    public HoppersConfig getCreationConfig() {
        return creationConfig;
    }

    /**
     * A helper function that builds the provided HoppersConfig into the format
     * of HoppersModel's string representation.
     *
     * @param buildString StringBuilder used to construct the resulting string
     * @param config the HoppersConfig whose state is represented
     * @return
     */
    public String formatHoppersConfig(StringBuilder buildString, HoppersConfig config) {
        for (int i = 0; i < config.getCol(); i++) {
            buildString.append(" ").append(i);
        }
        buildString.append("\n  ");
        buildString.append("-".repeat(config.getCol() * 2));

        // build side header & main Hoppers grid
        buildString.append("\n");
        String[] boardRows = config.toString().split("\n");
        for (int i = 0; i < config.getRow(); i++) {
            buildString.append(i).append("| ").append(boardRows[i])
                    .append("\n");
        }
        return buildString.toString();
    }


    /**
     * The string representation of the Hoppers puzzle with some added
     * row/column index visuals:
     * (example)
     * 0 1 2 3 4
     * ----------
     * 0| . * . * .
     * 1| * G * . *
     * 2| . * R * .
     * 3| * G * G *
     * 4| G * . * .
     *
     * @return the string in the format showcased above
     */
    @Override
    public String toString() {
        // create index header
        StringBuilder finalString = new StringBuilder("  ");
        if (!creationMode) {
            return formatHoppersConfig(finalString, currentConfig);
        } else {
            // in creation mode:
            return formatHoppersConfig(finalString, creationConfig);
        }
    }


}

