package core.hoppers.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
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

    /** PRIMARY VIEW COMPONENTS */
    private Stage stage;
    private BorderPane mainPane;
    private VBox titlePane;
    private GridPane grid;
    private Label displayLabel;
    private Button loadBtn;
    private Button resetBtn;
    private Button hintBtn;

    /** VISUAL ASSETS */
    private Image lilyPad = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"lily_pad.png"));
    private Image water = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"water.png"));
    // Pokémon assets!
    // > Politoed represents a Green Frog
    private Image poliPad = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"lily_padPolitoed.png"));
    private Image politoed = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"politoed.png"));
    // > A Pokeball represents the Red Frog
    private Image pokeball = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"lily_padSmallBall.png"));

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
        initializeGrid();
        updateGrid();
        mainPane.setCenter(grid);

        // > BOTTOM --- Contains back, load, reset, and hint buttons
        HBox bottBox = new HBox();
        HBox spaceBox = new HBox();
        BorderPane bottMenu = new BorderPane();
        bottBox.setAlignment(Pos.CENTER);

        loadBtn = new Button("Load");
        resetBtn = new Button("Reset");
        hintBtn = new Button("Hint");
        Button backBtn = new Button("Back");
        loadBtn.setTextAlignment(TextAlignment.CENTER);
        resetBtn.setTextAlignment(TextAlignment.CENTER);
        hintBtn.setTextAlignment(TextAlignment.CENTER);
        HBox leftBox = new HBox(backBtn);
        bottBox.getChildren().addAll(loadBtn, resetBtn, hintBtn);

        bottMenu.setLeft(leftBox);
        bottMenu.setCenter(bottBox);
        bottMenu.setRight(spaceBox);
        spaceBox.prefWidthProperty().bind(leftBox.widthProperty());
        mainPane.setBottom(bottMenu);


        // ******************* THE CONTROLLER ***************************

        loadBtn.setOnAction(e -> chooseFile(stage));

        resetBtn.setOnAction(e -> model.reset());

        hintBtn.setOnAction(e -> model.hint());

        // **************************************************************


        // Initialize the Stage
        stage.setTitle("Hoppers GUI");
        stage.setResizable(true);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setResizable(false);
        stage.show();
    }

    /**
     * A factory method that initializes the grid representing the Hoppers game
     * board according to the data stored in the model.
     */
    public void initializeGrid() {
        // Clear old state (if any)
        grid.getChildren().clear();
        grid.getColumnConstraints().clear();
        grid.getRowConstraints().clear();
        grid.setHgap(0);
        grid.setVgap(0);
        grid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        for (int col = 0; col < model.getBoard().getCol(); col++) {
            for (int row = 0; row < model.getBoard().getRow(); row++) {
                Button btn = new Button();
                // obscure button's visual state when pressed/hovered over
                btn.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-background-insets: 0");
                // a button's baseline size: each image will be 75x75 pixels!
                btn.setPrefSize(75,75);
                // allows expansion if there is any empty space in the cell
                btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                GridPane.setHgrow(btn, Priority.ALWAYS);
                GridPane.setVgrow(btn, Priority.ALWAYS);
                grid.add(btn, col, row);

            }
        }
        grid.setAlignment(Pos.CENTER);
        stage.sizeToScene();
        // ***** Controller initialization *****
        for (Node child : grid.getChildren()) {
            String r = String.valueOf(GridPane.getRowIndex(child));
            String c = String.valueOf(GridPane.getColumnIndex(child));
            Button btn = (Button) child;
            btn.setOnAction(e -> model.select(r, c));
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

            initializeGrid();
            // refresh borderPane's center & resize dimensions
            mainPane.setCenter(null);
            mainPane.setCenter(grid);
        } else {
            displayLabel.setText(msg);
        }
        updateGrid();
        stage.sizeToScene();
    }

    /**
     * Helper function that iterates over each cell in the GUI's GridPane and
     * updates it according to the grid state stored in the HoppersModel.
     */
    public void updateGrid() {
        for (Node node : grid.getChildren()) {
            int r = GridPane.getRowIndex(node);
            int c = GridPane.getColumnIndex(node);
            Button btn = (Button) node;

            Image graphic;
            graphic = switch (model.getBoard().getCell(r, c)) {
                case HoppersConfig.GREEN_FROG -> poliPad;
                case HoppersConfig.RED_FROG -> pokeball;
                case HoppersConfig.EMPTY_SPACE -> lilyPad;
                case HoppersConfig.INVALID_SPACE -> water;
                default -> throw new RuntimeException("Invalid cell value!");
            };
            ImageView img = new ImageView(graphic);
            img.setPreserveRatio(false);
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
        currentPath += File.separator + "data" + File.separator + "hoppers";
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

