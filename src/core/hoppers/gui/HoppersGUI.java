package core.hoppers.gui;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.stage.FileChooser;
import core.common.Observer;
import core.hoppers.model.HoppersConfig;
import core.hoppers.model.HoppersModel;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A graphical UI for the Hoppers puzzle game, replicating a pond
 * populated with the Pokémon Politoed in lieu of frogs!
 * Acts as both the view & controller, monitoring the model and updating itself
 * whenever the model's state changes.
 *
 * @author Tereza Lang (@TORITZA)
 */
public class HoppersGUI extends Application implements Observer<HoppersModel, String> {
    /** the model */
    private HoppersModel model;

    /** resources directory that is located directly underneath the GUI package */
    private final static String RESOURCES_DIR = "resources/";

    /** is the user currently placing something on the creation board? */
    boolean placing = false;

    /** PRIMARY VIEW COMPONENTS */
    private Stage stage;
    private BorderPane mainPane;
    private VBox titlePane;
    private GridPane grid;
    private Label displayLabel;
    private Button loadBtn;
    private Button resetBtn;
    private Button hintBtn;
    private Button createBtn;

    /** CREATION MODE VIEW COMPONENTS **/
    private BorderPane createPane;
    private GridPane customBoard;
    private Button redFrog;
    private Button greenFrog;
    private AtomicReference<String> frogType;
    private Button deleteBtn;
    private HBox leftContent;
    private Button saveBtn;
    private Button exitBtn;
    private Button cancelBtn;

    /** VISUAL ASSETS */
    private Image lilyPad = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"lily_pad.png"));
    private Image water = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"water.png"));
    // Pokémon assets!
    // > Politoed represents a Green Frog
    private Image poliPad = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"lily_padPolitoed.png"));
    private Image politoed = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"politoed.png"));
    // > A Pokeball represents the Red Frog
    private Image pokePad = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"lily_padSmallBall.png"));
    private Image pokeball = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"pokeball.png"));

    /**
     * Runs before start() & before the GUI is constructed.
     * Initializes the model and registers this GUI as its main observer.
     */
    @Override
    public void init() {
        // retrieve name of puzzle file from CMD-line arguments
        String filename = getParameters().getRaw().get(0);
        // load in the provided puzzle
        try {
            this.model = new HoppersModel(filename);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        // add this GUI to model's list of observers
        this.model.addObserver(this);
    }

    /**
     * Constructs the layout for the Hoppers puzzle.
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set
     * @throws Exception thrown if an error arises
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        stage = primaryStage;

        titleScreen();
    }

    /**
     * Displays the initial title screen upon program start-up.
     */
    public void titleScreen() {
        // Create VBox for title, buttons & character animation
        titlePane = new VBox();
        titlePane.setAlignment(Pos.CENTER);
        Scene scene = new Scene(titlePane);

        Label title = new Label("Hoppers");
        title.setStyle("-fx-font-size: 48px; -fx-padding: 8px;");
        title.setTextAlignment(TextAlignment.CENTER);

        Button playBtn = new Button("Play");
        Button helpBtn = new Button("Help");
        Button creditBtn = new Button("Credits");
        Button quitBtn = new Button("Quit");

            // animated Politoed asset on title screen!
        ImageView poliDancer = new ImageView(politoed);
        poliDancer.setFitHeight(160);
        poliDancer.setFitWidth(160);
        poliDancer.setPreserveRatio(true);
        HBox imageBox = new HBox(poliDancer);
        imageBox.setAlignment(Pos.CENTER);
        imageBox.setPadding(new Insets(12));


        // ******************* THE CONTROLLER ***************************

        playBtn.setOnAction(e -> mainScreen());

        // **************************************************************


        titlePane.setSpacing(8);
        titlePane.getChildren().addAll(title, playBtn, helpBtn, creditBtn, quitBtn, imageBox);
        titlePane.setPrefSize(376.0, 419.2);

        // Initialize the Stage
        stage.setTitle("Hoppers GUI");
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * WIP
     */
    public void helpMenu() {
        // WIP
        // How to Play
        // Report Bug
    }

    /**
     * Displays the primary Hoppers screen.
     */
    public void mainScreen() {
        // Create borderPane w/ intent to fill top, center, and bottom sections
        mainPane = new BorderPane();
        Scene scene = new Scene(mainPane);

        // > TOP --- Displaying game data to user
        HBox topBox = new HBox();
        displayLabel = new Label("Game start!");
        displayLabel.setTextAlignment(TextAlignment.CENTER);
        topBox.getChildren().add(displayLabel);
        topBox.setAlignment(Pos.CENTER);
        mainPane.setTop(topBox);


        // > CENTER (stretches to LEFT & RIGHT) --- Handles creation of Hoppers board
        grid = new GridPane();
        initializeGrid(grid);
        updateGrid(grid);
        mainPane.setCenter(grid);


        // > BOTTOM --- Contains back, load, reset, and hint buttons
        HBox bottBox = new HBox();
        HBox spaceBox = new HBox();
        BorderPane bottMenu = new BorderPane();
        bottBox.setAlignment(Pos.CENTER);

        loadBtn = new Button("Load");
        resetBtn = new Button("Reset");
        hintBtn = new Button("Hint");
        createBtn = new Button("Create");
        Button backBtn = new Button("Back");
        loadBtn.setTextAlignment(TextAlignment.CENTER);
        resetBtn.setTextAlignment(TextAlignment.CENTER);
        hintBtn.setTextAlignment(TextAlignment.CENTER);
        createBtn.setTextAlignment(TextAlignment.CENTER);
        HBox leftBox = new HBox(backBtn);
        bottBox.getChildren().addAll(loadBtn, resetBtn, hintBtn, createBtn);

        bottMenu.setLeft(leftBox);
        bottMenu.setCenter(bottBox);
        bottMenu.setRight(spaceBox);
        spaceBox.prefWidthProperty().bind(leftBox.widthProperty());
        mainPane.setBottom(bottMenu);


        // ******************* THE CONTROLLER ***************************

        backBtn.setOnAction(e -> {
            model.load("data/default/hoppers-4.txt");
            model.reset();
            stage.hide();
            titleScreen();
            stage.show();
                });

        loadBtn.setOnAction(e -> chooseFile(stage));

        resetBtn.setOnAction(e -> model.reset());

        hintBtn.setOnAction(e -> model.hint());

        createBtn.setOnAction(e -> dimPrompter());

        // **************************************************************


        // Initialize the Stage
        stage.setScene(scene);
        stage.setTitle("Hoppers GUI");
        stage.setResizable(true);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }

    /**
     * A factory method that initializes the grid representing the Hoppers game
     * board according to the data stored in the model.
     *
     * @param currentBoard depending on the model's state, this is the GridPane
     *                      that is to be initialized
     */
    public void initializeGrid(GridPane currentBoard) {
        HoppersConfig modelBoard;
        if (currentBoard == customBoard) {
            modelBoard = model.getCreationConfig();
        } else {
            modelBoard = model.getBoard();
        }

        // Clear old state (if any)
        currentBoard.getChildren().clear();
        currentBoard.getColumnConstraints().clear();
        currentBoard.getRowConstraints().clear();
        currentBoard.setHgap(0);
        currentBoard.setVgap(0);
        currentBoard.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        for (int col = 0; col < modelBoard.getCol(); col++) {
            for (int row = 0; row < modelBoard.getRow(); row++) {
                Button btn = new Button();
                // obscure button's visual state when pressed/hovered over
                btn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-background-insets: 0");
                // a button's baseline size: each image will be 75x75 pixels!
                btn.setPrefSize(75,75);
                // allows expansion if there is any empty space in the cell
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                GridPane.setHgrow(btn, Priority.ALWAYS);
                GridPane.setVgrow(btn, Priority.ALWAYS);
                currentBoard.add(btn, col, row);

            }
        }
        currentBoard.setAlignment(Pos.CENTER);

        if (currentBoard == grid) {
            // ***** Controller initialization for main play mode *****
            for (Node child : grid.getChildren()) {
                String r = String.valueOf(GridPane.getRowIndex(child));
                String c = String.valueOf(GridPane.getColumnIndex(child));
                Button btn = (Button) child;
                btn.setOnAction(e -> model.select(r, c));
            }
            } else {
                for (Node child : customBoard.getChildren()) {
                    String r = String.valueOf(GridPane.getRowIndex(child));
                    String c = String.valueOf(GridPane.getColumnIndex(child));
                    Button btn = (Button) child;

                    btn.setOnAction(e -> {
                        if (placing) {
                            model.place(r, c, frogType.get());
                            updateGrid(customBoard);
                            placementLock(false);
                            placing = false;
                        } else {
                            displayLabel.setText("First select a game piece to place it onto the board!");
                        }
                        });
                }
            }
    }

    /**
     * Prompts to user for the dimensions of the board they wish to create. Then, creates
     * the custom puzzle with the values provided, instantiated in HoppersModel.
     */
    public void dimPrompter() {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Custom Board Size");
        dialog.setHeaderText("Please enter the dimensions of the puzzle you wish to create!");

        // set button types
        ButtonType continueButtonType = new ButtonType("Continue", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(continueButtonType, ButtonType.CANCEL);

        // configure text fields
        TextField row = new TextField();
        TextField col = new TextField();

        GridPane diaLayout = new GridPane();
        diaLayout.setHgap(10);
        diaLayout.setVgap(10);
        diaLayout.add(new Label("Row(s):"), 0, 0);
        diaLayout.add(row, 1, 0);
        diaLayout.add(new Label("Column(s):"), 0, 1);
        diaLayout.add(col, 1, 1);

        dialog.getDialogPane().setContent(diaLayout);

        // convert results to list
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == continueButtonType) {
                return new String[]{row.getText(), col.getText()};
            }
            return null;
        });

        // convert continue type to button & check bounds of text fields
        Button continueButton = (Button) dialog.getDialogPane().lookupButton(continueButtonType);
        continueButton.addEventFilter(ActionEvent.ACTION, e -> {
            // check if entries are valid
            try {
                int r = Integer.parseInt(row.getText().trim());
                int c = Integer.parseInt(col.getText().trim());

                if (r <= 0 || c <= 0) {
                    dialog.setHeaderText("Both fields must be greater than zero.");
                    e.consume();
                } else if (r * c > 144) {
                    dialog.setHeaderText("Too large! Please ensure the board has less than 145 tiles.");
                    e.consume();
                }
            } catch (NumberFormatException n) {
                dialog.setHeaderText("Please enter valid, whole numbers.");
                e.consume();
            }
        });

        Optional<String[]> results = dialog.showAndWait();
        results.ifPresent(args -> {
            model.create(args[0], args[1]);
            createMode();
            displayLabel.setText("Welcome to Creation Mode!");
        });
    }

    /**
     * Prompts the user for confirmation given that they wish to exit Creation Mode
     * and return to the main screen. If yes, displays the main puzzle screen to
     * the user.
     */
    public void exitDialog() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle(null);
        confirmDialog.setHeaderText("Exiting Creation Mode will result in losing the progress you've made so far.");
        confirmDialog.setContentText("Are you certain you wish to leave?");
            // update l8tr?!
        confirmDialog.setGraphic(null);

        // replace default alert btns
        confirmDialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirmDialog.showAndWait();
            // user confirms & clicks Yes
        if (result.isPresent() && result.get() == ButtonType.YES) {
            model.reset();
            stage.hide();
            mainScreen();
            model.load("data/default/hoppers-4.txt");
            displayLabel.setText("Welcome back!");
            stage.show();
            model.toggleCreationMode();
        }
            // user clicks No/exits out of prompter -> Do nothing
    }

    /**
     *
     */
    public void backDialog() {

    }


    /**
     * Displays the screen for Creation Mode, where users can produce their own
     * Hoppers puzzles.
     */
    public void createMode() {
        model.toggleCreationMode();
        createPane = new BorderPane();
        Scene scene = new Scene(createPane);

        // > TOP --- Displaying game data to user
        HBox topBox = new HBox();
        displayLabel = new Label();
        displayLabel.setTextAlignment(TextAlignment.CENTER);
        topBox.getChildren().add(displayLabel);
        topBox.setAlignment(Pos.CENTER);
        createPane.setTop(topBox);


        // > LEFT --- State of creation board & Hoppers pieces
        VBox redFrogs = new VBox();
        Label redCount = new Label("Red: " + model.getRedFrogCount());
        Label maxRedCount = new Label("Max: 1");
        redCount.setTextAlignment(TextAlignment.CENTER);
        maxRedCount.setTextAlignment(TextAlignment.CENTER);
        redFrogs.setAlignment(Pos.CENTER);
        redFrogs.getChildren().addAll(redCount, maxRedCount);

        VBox greenFrogs = new VBox();
        Label greenCount = new Label("Green: " + model.getGreenFrogCount());
        Label maxGreenCount = new Label("Max: " + model.getMaxGreenFrogs());
        greenCount.setTextAlignment(TextAlignment.CENTER);
        maxGreenCount.setTextAlignment(TextAlignment.CENTER);
        greenFrogs.setAlignment(Pos.CENTER);
        greenFrogs.getChildren().addAll(greenCount, maxGreenCount);
        HBox frogCounters = new HBox();
        frogCounters.setAlignment(Pos.CENTER);
        frogCounters.setSpacing(12);
        frogCounters.setPadding(new Insets(4, 0, 8, 0));
        frogCounters.getChildren().addAll(redFrogs, greenFrogs);

        VBox puzzlePieces = new VBox(); // instruction text + buttons for red & green frogs
        Label instructions = new Label("Click or drag each piece to \nplace it onto an empty lilypad!");
        instructions.setPadding(new Insets(0, 0, -8, 0));

       greenFrog = new Button();
        ImageView greenFrogGraphic = new ImageView(politoed);
        greenFrogGraphic.setPreserveRatio(true);
        greenFrogGraphic.setFitHeight(90);
        greenFrogGraphic.setFitWidth(90);
        greenFrog.setGraphic(greenFrogGraphic);

        redFrog = new Button();
        ImageView redFrogGraphic = new ImageView(pokeball);
        redFrogGraphic.setFitHeight(50);
        redFrogGraphic.setFitWidth(50);
        redFrog.setGraphic(redFrogGraphic);

        HBox delete = new HBox();
        Label deleteLbl = new Label("Delete: ");
        deleteBtn = new Button("x"); // X
        delete.setAlignment(Pos.CENTER);
        delete.setSpacing(2);
        delete.getChildren().addAll(deleteLbl, deleteBtn);

        puzzlePieces.setAlignment(Pos.CENTER);
        puzzlePieces.setSpacing(24);
        puzzlePieces.setPadding(new Insets(12, 0, 0, 0));
        puzzlePieces.getChildren().addAll(instructions, redFrog, greenFrog, delete);

        VBox leftBox = new VBox();
        leftBox.getChildren().addAll(frogCounters, puzzlePieces);
        leftBox.setPadding(new Insets(8));
        createPane.setLeft(leftBox);


        // > CENTER --- Houses custom board
        customBoard = new GridPane();
        initializeGrid(customBoard);
        updateGrid(customBoard);
        customBoard.setMaxSize(90, 90);
        customBoard.setPadding(new Insets(20, 20, 20, 12));
        createPane.setCenter(customBoard);


        // > BOTTOM --- Contains exit and save buttons
        BorderPane bottMenu = new BorderPane();
        exitBtn = new Button("Exit");
        leftContent = new HBox(exitBtn);
        saveBtn = new Button("Save");
        HBox bottBox = new HBox(saveBtn);
        bottBox.setAlignment(Pos.CENTER);
        HBox spaceBox = new HBox();

        bottMenu.setLeft(leftContent);
        bottMenu.setCenter(bottBox);
        bottMenu.setRight(spaceBox);
        spaceBox.prefWidthProperty().bind(leftContent.widthProperty());
        createPane.setBottom(bottMenu);

        cancelBtn = new Button("Cancel");


        // ******************* THE CONTROLLER ***************************

        frogType = new AtomicReference<>("");

        redFrog.setOnAction(e -> {
            placing = true;
            frogType.set("R");
            placementLock(placing);
        });

        greenFrog.setOnAction(e -> {
            placing = true;
            frogType.set("G");
            placementLock(placing);
        });

        exitBtn.setOnAction(e -> exitDialog());

        cancelBtn.setOnAction(e -> {
            placing = false;
            placementLock(false);
            displayLabel.setText("Placement canceled");
        });

        saveBtn.setOnAction(e -> model.save());

        // **************************************************************


        createPane.setPrefSize(376.0, 419.2);

        // Initialize the Stage
        stage.setTitle("Hoppers GUI");
        stage.setResizable(true);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }

    /**
     * If the user is currently placing a frog on the creation board, lock all other
     * functionality on creation screen; if not, display each view component as normal.
     *
     * @param entered has the user entered a state in which they're placing pieces
     *                onto the creation board?
     */
    public void placementLock(boolean entered) {
        if (entered) {
            redFrog.setDisable(true);
            greenFrog.setDisable(true);
            saveBtn.setVisible(false);
            saveBtn.setDisable(true);
            deleteBtn.setDisable(true);
            leftContent.getChildren().set(0, cancelBtn);
            displayLabel.setText("Choose a space!");
        } else {
            redFrog.setDisable(false);
            greenFrog.setDisable(false);
            saveBtn.setVisible(true);
            saveBtn.setDisable(false);
            deleteBtn.setDisable(false);
            leftContent.getChildren().set(0, exitBtn);
        }
    }

    /**
     * Called by the HoppersModel whenever there is a state change
     * that needs to be updated by the GUI.
     *
     * @param hoppersModel informs the view that the internal logic has changed
     *                     and the external display should reflect that
     * @param msg data the model sends to this observer
     *
     */
    @Override
    public void update(HoppersModel hoppersModel, String msg) {
        if (msg.contains("Loaded")) {
            // safely display file name, truncating the path used to retrieve it
            String filePath = msg.replace("Loaded: ", "").strip();
            File file = new File(filePath);
            displayLabel.setText("Loaded: " + file.getName());

            initializeGrid(grid);
            // refresh borderPane's center & resize dimensions
            mainPane.setCenter(null);
            mainPane.setCenter(grid);
        } else {
            displayLabel.setText(msg);
        }
        if (model.getCreationStatus()) {
            updateGrid(customBoard);
        } else {
            updateGrid(grid);
        }
        stage.sizeToScene();
    }

    /**
     * Helper function that iterates over each cell in the GUI's GridPane and
     * updates it according to the grid state stored in the HoppersModel.
     *
     * @param currentBoard depending on the model's state, this is the GridPane
     *                     that is to be updated
     */
    public void updateGrid(GridPane currentBoard) {
        HoppersConfig modelBoard;
        if (currentBoard == customBoard) {
            modelBoard = model.getCreationConfig();
        } else {
            modelBoard = model.getBoard();
        }

        for (Node node : currentBoard.getChildren()) {
            int r = GridPane.getRowIndex(node);
            int c = GridPane.getColumnIndex(node);

            Button btn = (Button) node;

            Image graphic;
            graphic = switch (modelBoard.getCell(r, c)) {
                case HoppersConfig.GREEN_FROG -> poliPad;
                case HoppersConfig.RED_FROG -> pokePad;
                case HoppersConfig.EMPTY_SPACE -> lilyPad;
                case HoppersConfig.INVALID_SPACE -> water;
                default -> throw new RuntimeException("Invalid cell value!");
            };
            ImageView img = new ImageView(graphic);
            img.setPreserveRatio(true);
            img.setSmooth(true);
            // binds image to the button's size
            img.fitWidthProperty().bind(btn.widthProperty());
            img.fitHeightProperty().bind(btn.heightProperty());

            btn.setGraphic(img);
        }
    }

    /**
     * A helper method for prompting for & opening a file from the user's desktop
     * to be used in the creation of a Hoppers puzzle.
     * @param stage the GUI's primary stage
     */
    public void chooseFile(Stage stage) {
        FileChooser chooser = new FileChooser();
        String currentPath = Paths.get(".").toAbsolutePath().normalize().toString();
        currentPath += File.separator + "data";
        chooser.setInitialDirectory(new File(currentPath));

        chooser.setTitle("Select Hoppers File");
        File chosenFile = chooser.showOpenDialog(stage);
        model.load(chosenFile.getAbsolutePath());
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java HoppersPTUI filename");
        } else {
            Application.launch(args);
        }
    }
}

