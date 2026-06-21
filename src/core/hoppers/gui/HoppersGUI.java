package core.hoppers.gui;

import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import core.common.Observer;
import core.hoppers.model.HoppersConfig;
import core.hoppers.model.HoppersModel;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.util.Duration;

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
    /** is the user currently deleting something on the creation board? */
    boolean deleting = false;

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
    private Label redCount;
    private Label greenCount;
    private Button redFrog;
    private Button greenFrog;
    private AtomicReference<String> frogType;
    private Button deleteBtn = new Button();
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
        // > For alert dialogs; made by @rotten_ichor
    private Image poliQuestion = new Image(getClass().getResourceAsStream(RESOURCES_DIR+"poliQuestion.png"),
            105, 105, true, true);
    private ImageView poliPrompter = new ImageView(poliQuestion);
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
        playBtn.setFocusTraversable(false);
        Button helpBtn = new Button("Help");
        helpBtn.setFocusTraversable(false);
        Button creditBtn = new Button("Credits");
        creditBtn.setFocusTraversable(false);
        Button quitBtn = new Button("Quit");
        quitBtn.setFocusTraversable(false);

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

        helpBtn.setOnAction(e -> helpMenu());

        quitBtn.setOnAction(e -> Platform.exit());

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
     *
     * @param img
     */
    private void titleAnimation(ImageView img) {
        // vertical bounce/jump
        TranslateTransition bounce = new TranslateTransition(Duration.millis(600), img);
    }

    /**
     * Showcases the screen with various "help" options that a user may seek
     * assistance with.
     */
    public void helpMenu() {
        BorderPane help = new BorderPane();
        help.setPrefSize(376.0, 419.2);
        Scene scene = new Scene(help);

        Button howToPlay = new Button("How to Play");
        howToPlay.setFocusTraversable(false);
        howToPlay.setTextAlignment(TextAlignment.CENTER);
        Button controls = new Button("Controls");
        controls.setFocusTraversable(false);
        controls.setTextAlignment(TextAlignment.CENTER);
        Button whatIs = new Button("What is Creation Mode");
        whatIs.setFocusTraversable(false);
        whatIs.setTextAlignment(TextAlignment.CENTER);
        Button reportBug = new Button("Report Bug");
        reportBug.setTextAlignment(TextAlignment.CENTER);
        reportBug.setFocusTraversable(false);
        VBox menu = new VBox(howToPlay, controls, whatIs, reportBug);
        menu.setAlignment(Pos.CENTER);
        menu.setSpacing(14);

        Button backBtn = new Button("Back");
        backBtn.setFocusTraversable(false);
        HBox back = new HBox(backBtn);
        back.setAlignment(Pos.CENTER);
        back.setPadding(new Insets(0, 0, 12, 0));

        help.setCenter(menu);
        help.setBottom(back);

        // ******************** CONTROLLER ********************

        howToPlay.setOnAction(e -> howToPlayScreen());

        controls.setOnAction(e -> controlsScreen());

        whatIs.setOnAction(e -> whatIsScreen());

        backBtn.setOnAction(e -> titleScreen());

        // ***************************************************

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }


    /**
     * Displays information on the rules of the Hoppers game.
     */
    public void howToPlayScreen() {
        BorderPane howToPane = new BorderPane();
        Scene scene = new Scene(howToPane);

        Label quote = new Label("Survey the pond, then jump frogs until only \n" +
                "ONE is left standing...Start simple and grow your \n" +
                "skills with each level. In no time at all you’ll be \n" +
                "the smartest frog in the pond!\n" +
                "                       -- Official Hoppers Manual ");
        quote.setOpacity(20);
        quote.setWrapText(true);
        quote.setTextAlignment(TextAlignment.CENTER);
        VBox quoteBox = new VBox(quote);
        quote.setStyle("-fx-border-width: 1px; -fx-border-color: #D3D3D3");

        Text intro1 = new Text("Hoppers is a logic puzzle—to be more specific, " +
                "a single-player peg-solitaire game—produced by the popular toy and board game company ThinkFun " +
                "but invented by Nob Yoshigahara. However, in this iteration, you'll notice that the frog leaping " +
                "isn't your typical amphibian—it's Politoed, the Frog ");
        Text italicIntro = new Text("Pokémon");
        italicIntro.setStyle("-fx-font-style: italic");
        Text intro2 = new Text("!");
        TextFlow completeIntro = new TextFlow(intro1, italicIntro, intro2);
        Label intro = new Label();
        intro.setGraphic(completeIntro);
        intro.setWrapText(true);

        Label preface = new Label("\nThe rules nevertheless remain the same:");

        Label bulletItem1 = new Label("\t• Politoeds, which stand-in for the original's Green frogs, and " +
                "the Pokéball, representing the Red frog, may only jump from lily pad to lily pad cardinally and " +
                "intercardinally—that is, North, East, South, West and the diagonals.");
        bulletItem1.setWrapText(true);

        Label bulletItem2 = new Label("\t• Additionally, in order for either frog type to move, another Politoed " +
                "must rest adjacently to as well as in-between them and the space they wish to hop to. If executed" +
                " successfully, it will \"capture\" and remove that Politoed off the board!");
        bulletItem2.setWrapText(true);

        Label bulletItem3 = new Label();
        Text bulletStart3 = new Text("\t• Neither a Politoed nor a Pokéball can leap into water, over an " +
                "empty lily pad, onto another Politoed, or over two at once. Furthermore, as the game's win " +
                "condition, the Pokéball is special in the sense that it cannot be captured; it ");
        Text italic3 = new Text("must");
        Text bulletEnd3 = new Text(" remain on the board, and no other Politoed can hop over and remove it.");
        italic3.setStyle("-fx-font-style: italic");
        TextFlow bulletFlow3 = new TextFlow(bulletStart3, italic3, bulletEnd3);
        bulletItem3.setGraphic(bulletFlow3);
        bulletItem3.setWrapText(true);

        Label bulletItem4 = new Label("\t• The trainer (that's you!) wins when the Pokéball is the lone piece" +
                " on the board, indicating that all Politoed once present have been caught.");
        bulletItem4.setWrapText(true);

        Label explanation = new Label("\nInitially, a 5x5 Hoppers puzzle is loaded in. Although not all the" +
                " default puzzles provided are solvable, a good chunk of them are. It's up to you to determine" +
                " how to approach each and ensure your Pokéball is the last piece standing.\n");
        explanation.setWrapText(true);

        Label encouragement = new Label("\nNow, go catch 'em all, and become the best Politoed wrangler" +
                " there ever was!");
        encouragement.setWrapText(true);


        VBox content = new VBox(intro, preface, bulletItem1, bulletItem2, bulletItem3, bulletItem4,
                explanation, encouragement);

        ScrollPane scrollBox = new ScrollPane();
        scrollBox.setPannable(true);
        scrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollBox.setFitToWidth(true);
        quoteBox.setAlignment(Pos.CENTER);
        quoteBox.setPadding(new Insets(8,8,0,8));
        quote.setPadding(new Insets(6));
        content.setFillWidth(false);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(10));
        VBox all = new VBox(quoteBox, content);
        scrollBox.setContent(all);
        howToPane.setCenter(scrollBox); 

        Button backBtn = new Button("Back");
        backBtn.setFocusTraversable(false);
        HBox bottMenu = new HBox(backBtn);
        bottMenu.setAlignment(Pos.CENTER);
        bottMenu.setPadding(new Insets(8, 0, 8, 0));
        quoteBox.setAlignment(Pos.CENTER);
        howToPane.setBottom(bottMenu);

        // ********************* CONTROLLER *************************

        backBtn.setOnAction(e -> helpMenu());

        // **********************************************************

        howToPane.setPrefSize(376.0, 419.2);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     *
     */
    public void controlsScreen() {
        BorderPane controlsPane = new BorderPane();
        Scene scene = new Scene(controlsPane);

        Label intro = new Label();
        Text intro1 = new Text("Upon clicking the ");
        Text boldIntro = new Text("Play");
        boldIntro.setStyle("-fx-font-weight: bold");
        Text intro2 = new Text(" button, you'll notice that the main screen houses six key controls you can " +
                "use to navigate the puzzle in front of you (or even create another!).\n");
        TextFlow completeIntro = new TextFlow(intro1, boldIntro, intro2);
        intro.setGraphic(completeIntro);
        intro.setWrapText(true);

        // How to move pieces on the Hoppers board
        Label moveControls = new Label();
        Text move1 = new Text("Firstly, to move a piece on the board, begin by using your cursor to select the" +
                " one you wish to position and clicking on its destination. Don't worry about memorizing any of" +
                " the constraints imposed on you by the game itself; should you want to read up on and be cognizant" +
                " of the rules, there is always the ");
        Text boldMove = new Text("How To Play");
        boldMove.setStyle("-fx-font-weight: bold");
        Text move2 = new Text(" tab, but the app also notifies you ");
        Text moveItalic1 = new Text("if");
        moveItalic1.setStyle("-fx-font-style: italic");
        Text move3 = new Text(" and ");
        Text moveItalic2 = new Text("why");
        moveItalic2.setStyle("-fx-font-style: italic");
        Text move4 = new Text(" a move you made is invalid. It even provides feedback confirming a move is " +
                "successful!\n");
        TextFlow moveComplete = new TextFlow(move1, boldMove, move2, moveItalic1, move3, moveItalic2, move4);
        moveControls.setGraphic(moveComplete);
        moveControls.setWrapText(true);

        Label buttonIntro = new Label("\nThe functions of the other aforementioned controls are teased through " +
                "their titles:");
        buttonIntro.setWrapText(true);

        // the Load button
        Label loadButton = new Label();
        Text load1 = new Text("\nThe ");
        Text boldLoad1 = new Text("Load");
        boldLoad1.setStyle("-fx-font-weight: bold");
        Text load2 = new Text(" button opens a dialog box, prompting you to choose a file from either the ");
        Text loadItalic1 = new Text("default");
        loadItalic1.setStyle("-fx-font-style: italic");
        Text load3 = new Text(" or ");
        Text loadItalic2 = new Text("custom");
        loadItalic2.setStyle("-fx-font-style: italic");
        Text load4 = new Text(" folder. The former holds standard, ready-made Hoppers puzzles, while the latter " +
                "carries any puzzles you might've configured in ");
        Text boldLoad2 = new Text("Creation Mode");
        boldLoad2.setStyle("-fx-font-weight: bold");
        Text load5 = new Text(". The app then proceeds to load your selection onto the main screen, priming it " +
                "for your play.\n");
        TextFlow loadComplete = new TextFlow(load1, boldLoad1, load2, loadItalic1, load3, loadItalic2, load4,
                boldLoad2, load5);
        loadButton.setGraphic(loadComplete);
        loadButton.setWrapText(true);

        // the Reset button
        Label resetButton = new Label();
        Text reset1 = new Text("The ");
        Text resetBold = new Text("Reset");
        resetBold.setStyle("-fx-font-weight: bold");
        Text reset2 = new Text(" button positions the puzzle to how it first appeared. That is to say, it resets " +
                "the puzzle to its starting configuration, before you moved any pieces. Use this button to bail " +
                "you out of dead-end board states!\n");
        TextFlow resetComplete = new TextFlow(reset1, resetBold, reset2);
        resetButton.setGraphic(resetComplete);
        resetButton.setWrapText(true);

        // the Hint button
        Label hintButton = new Label();
        Text hint1 = new Text("Should you press the ");
        Text hintBold = new Text("Hint");
        hintBold.setStyle("-fx-font-weight: bold");
        Text hint2 = new Text(" button, the Hoppers board will automatically orient itself as if the next best " +
                "possible move was made. If the puzzle can no longer be solved given the location of each " +
                "remaining piece on the board, it will output that the puzzle, in its current state, is insoluble." +
                "\n");
        TextFlow hintComplete = new TextFlow(hint1, hintBold, hint2);
        hintButton.setGraphic(hintComplete);
        hintButton.setWrapText(true);

        // the Create button
        Label createButton = new Label();
        Text create1 = new Text("The ");
        Text createBold1 = new Text("Create");
        createBold1.setStyle("-fx-font-weight: bold");
        Text create2 = new Text(" button enters you into the app's ");
        Text createBold2 = new Text("Creation Mode");
        createBold2.setStyle("-fx-font-weight: bold");
        Text create3 = new Text(", which has its own dedicated section within the Help menu—one that also " +
                "details its own control scheme!\n");
        TextFlow createComplete = new TextFlow(create1, createBold1, create2, createBold2, create3);
        createButton.setGraphic(createComplete);
        createButton.setWrapText(true);

        // the Back button
        Label backButton = new Label();
        Text back1 = new Text("Lastly, the ");
        Text backBold = new Text("Back");
        backBold.setStyle("-fx-font-weight: bold");
        Text back2 = new Text(" button does precisely what it suggests: take you back to the title screen!" +
                " However, doing so will reset your progress on the current Hoppers puzzle and load in the" +
                " default one, should you decide to play again. This reminder will be reiterated through an" +
                " alert box, asking for your confirmation to exit back to the title screen.");
        TextFlow backComplete = new TextFlow(back1, backBold, back2);
        backButton.setGraphic(backComplete);
        backButton.setWrapText(true);


        VBox content = new VBox(intro, moveControls, buttonIntro, loadButton, resetButton, hintButton,
                backButton);
        ScrollPane scrollBox = new ScrollPane();
        scrollBox.setPannable(true);
        scrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollBox.setFitToWidth(true);
        content.setFillWidth(false);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-text-fill: #393939");
        scrollBox.setContent(content);
        controlsPane.setCenter(scrollBox);

        Button backBtn = new Button("Back");
        backBtn.setFocusTraversable(false);
        HBox bottMenu = new HBox(backBtn);
        bottMenu.setAlignment(Pos.CENTER);
        bottMenu.setPadding(new Insets(8, 0, 8, 0));
        controlsPane.setBottom(bottMenu);

        // ********************* CONTROLLER *************************

        backBtn.setOnAction(e -> helpMenu());

        // **********************************************************

        controlsPane.setPrefSize(376.0, 419.2);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     *
     */
    public void whatIsScreen() {
        BorderPane whatIsPane = new BorderPane();
        Scene scene = new Scene(whatIsPane);

        Label preamble = new Label("Puzzles cannot be played if no more are made; therefore, " +
                "Creation Mode is designed for players to explore new board setups and create their own, unique " +
                "puzzles!\n\tBelow are the instructions and additional information on how to do so:\n");
        preamble.setWrapText(true);

        Label firstPart = new Label();
        Text first1 = new Text("\n\tUpon pressing the ");
        Text firstBold = new Text("Create");
        firstBold.setStyle("-fx-font-weight: bold");
        Text first2 = new Text(" button and entering Creation Mode, the user will be prompted on the dimensions of " +
                "their Hoppers board. Should the board qualify as small enough (being less than 145 tiles), then " +
                "they will be carried to the creation screen and met with an unpopulated Hoppers pond on its " +
                "right-hand side, containing only lily pad and water tiles.");
        TextFlow firstComplete = new TextFlow(first1, firstBold, first2);
        firstPart.setGraphic(firstComplete);
        firstPart.setWrapText(true);

        Label secondPart = new Label("\tOn the left-hand side of the screen are the board pieces as well as " +
                "text reminding the user of the puzzle's constraint—that is, the number of red, Pokéball pieces " +
                "allowed, and the number of green, Politoed pieces allowed. If this amount is exceeded, the screen" +
                " will lock down any further placement of that specific frog type.");
        secondPart.setWrapText(true);

        Label thirdPart = new Label();
        Text third1 = new Text("\tThe only way to unlock and backtrack from this screen state is to delete a piece " +
                "of that frog type off the board using the ");
        Text thirdBold = new Text("Delete");
        thirdBold.setStyle("-fx-font-weight: bold");
        Text third2 = new Text(" button. Moreover, the program will also prevent you from placing frogs in water!");
        TextFlow thirdComplete = new TextFlow(third1, thirdBold, third2);
        thirdPart.setGraphic(thirdComplete);
        thirdPart.setWrapText(true);

        Label fourthPart = new Label();
        Text fourth1 = new Text("\tTo download the puzzle you created and save it later for active play, press " +
                "the ");
        Text fourthBold = new Text("Save");
        fourthBold.setStyle("-fx-font-weight: bold");
        Text fourth2 = new Text(" button when you've configured the creation board to your liking. This " +
                "will save the puzzle to the ");
        Text fourthItalic = new Text("custom");
        fourthItalic.setStyle("-fx-font-style: italic");
        Text fourth3 = new Text(" subdirectory, which is accesible upon loading in a Hoppers puzzle to play.");
        TextFlow fourthComplete = new TextFlow(fourth1, fourthBold, fourth2, fourthItalic, fourth3);
        fourthPart.setGraphic(fourthComplete);
        fourthPart.setWrapText(true);

        Label fifthPart = new Label();
        Text fifth1 = new Text("\tUsing the ");
        Text fifthBold = new Text("Back");
        fifthBold.setStyle("-fx-font-weight: bold");
        Text fifth2 = new Text(" button will first caution you about your current progress being lost" +
                " before taking you back to the main screen.");
        TextFlow fifthComplete = new TextFlow(fifth1, fifthBold, fifth2);
        fifthPart.setGraphic(fifthComplete);
        fifthPart.setWrapText(true);

        VBox content = new VBox(preamble, firstPart, secondPart, thirdPart, fourthPart, fifthPart);
        ScrollPane scrollBox = new ScrollPane();
        scrollBox.setPannable(true);
        scrollBox.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        scrollBox.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollBox.setFitToWidth(true);
        content.setFillWidth(false);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(10));
        content.setStyle("-fx-text-fill: #393939");
        scrollBox.setContent(content);
        whatIsPane.setCenter(scrollBox);

        Button backBtn = new Button("Back");
        backBtn.setFocusTraversable(false);
        HBox bottMenu = new HBox(backBtn);
        bottMenu.setAlignment(Pos.CENTER);
        bottMenu.setPadding(new Insets(8, 0, 8, 0));
        whatIsPane.setBottom(bottMenu);

        // ********************* CONTROLLER *************************

        backBtn.setOnAction(e -> helpMenu());

        // **********************************************************

        whatIsPane.setPrefSize(376.0, 419.2);

        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    /**
     * WIP
     */
    public void creditsScreen() {
        // Contributions: @rotten_ichor for alert dialog asset, pokeball png source, attribute Politoed
        // asset & usage of IP to Pokémon
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

        backBtn.setOnAction(e -> backDialog());

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
    private void initializeGrid(GridPane currentBoard) {
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
                GridPane.setHgrow(btn, Priority.NEVER);
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


                    //------------------------ON CLICK------------------------

                    btn.setOnAction(e -> {
                        if (placing) {
                            model.place(r, c, frogType.get());
                            updateGrid(customBoard);
                            placementLock(false);
                            placing = false;
                        } else {
                            displayLabel.setText("First select a game piece to place it onto the board!");
                        }

                        if (deleting) {
                            model.delete(r, c);
                            updateGrid(customBoard);
                            deleting = false;
                        }
                        });


                    // -------------------------ON DRAG--------------------------

                    btn.setOnDragOver(e -> {
                        e.acceptTransferModes(TransferMode.MOVE);
                        e.consume();
                    });

                    // visualization on hover
                    btn.setOnDragEntered(e -> {
                        ImageView baseLayer = switch (model.getCreationConfig().
                                getCell(Integer.parseInt(r), Integer.parseInt(c))) {
                            case HoppersConfig.EMPTY_SPACE -> new ImageView(lilyPad);
                            case HoppersConfig.INVALID_SPACE -> new ImageView(water);
                            default -> new ImageView(water);
                        };
                        baseLayer.fitWidthProperty().bind(btn.widthProperty());
                        baseLayer.fitHeightProperty().bind(btn.heightProperty());

                        ImageView overlay = switch (e.getDragboard().getString()) {
                            case "G" -> new ImageView(politoed);
                            case "R" -> new ImageView(pokeball);
                            default -> {
                                try {
                                    throw new Exception();
                                } catch (Exception ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                        };

                        overlay.setSmooth(true);
                        overlay.setPreserveRatio(true);
                        if (e.getDragboard().getString().equals("G")) {
                            overlay.fitWidthProperty().bind(btn.widthProperty());
                            overlay.fitHeightProperty().bind(btn.widthProperty());
                        } else {
                            overlay.setFitHeight(45);
                            overlay.setFitWidth(45);
                        }
                        overlay.setOpacity(.4); // 40% solid

                        StackPane stackedImg = new StackPane(baseLayer, overlay);
                        btn.setGraphic(stackedImg);
                        e.consume();
                    });

                    btn.setOnDragExited(e -> {
                        ImageView base = switch (model.getCreationConfig().
                                getCell(Integer.parseInt(r), Integer.parseInt(c))) {
                            case HoppersConfig.EMPTY_SPACE -> new ImageView(lilyPad);
                            case HoppersConfig.INVALID_SPACE -> new ImageView(water);
                            case HoppersConfig.RED_FROG -> new ImageView(pokePad);
                            case HoppersConfig.GREEN_FROG -> new ImageView(poliPad);
                            default -> throw new IllegalStateException("Unexpected value: " + model.getCreationConfig().
                                getCell(Integer.parseInt(r), Integer.parseInt(c)));
                        };
                        base.fitWidthProperty().bind(btn.widthProperty());
                        base.fitHeightProperty().bind(btn.heightProperty());
                        btn.setGraphic(base);
                        e.consume();
                    });

                    btn.setOnDragDropped(e -> {
                        Dragboard db = e.getDragboard();

                        String frogType = db.getString();
                        model.place(r, c, frogType);
                        updateGrid(customBoard);

                        e.setDropCompleted(true);
                        e.consume();

                    });
                }
            }
    }

    /**
     * Prompts to user for the dimensions of the board they wish to create. Then, creates
     * the custom puzzle with the values provided, instantiated in HoppersModel.
     */
    private void dimPrompter() {
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
    private void exitDialog() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle(null);
        confirmDialog.setHeaderText("Exiting Creation Mode will result in losing the progress you've made so far.");
        confirmDialog.setContentText("Are you certain you wish to leave?");
        confirmDialog.setGraphic(poliPrompter);

        // replace default alert btns
        confirmDialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirmDialog.showAndWait();
            // user confirms & clicks Yes
        if (result.isPresent() && result.get() == ButtonType.YES) {
            model.reset();
            stage.hide();
            deleting = false;
            mainScreen();
            model.load("data/default/hoppers-4.txt");
            displayLabel.setText("Welcome back!");
            stage.show();
            model.toggleCreationMode();
        }
            // user clicks No/exits out of prompter -> Do nothing
    }

    /**
     * Creates an alert box that waits on the user for confirmation if they wish to return
     * to the Hoppers title screen, resetting the main puzzle in the process.
     */
    private void backDialog() {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle(null);
        confirmDialog.setHeaderText("Returning to the title screen will result in losing " +
                "the progress you've made so far.");
        confirmDialog.setContentText("Are you sure you want to return?");
            // update l8tr!
        confirmDialog.setGraphic(poliPrompter);

        // replace default alert btns
        confirmDialog.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

        Optional<ButtonType> result = confirmDialog.showAndWait();
            // user clicks & confirms YES
        if (result.isPresent() && result.get() == ButtonType.YES) {
            stage.hide();
            model.reset();
            titleScreen();
            model.load("data/default/hoppers-4.txt");
        }
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
        redCount = new Label("Red: 0");
        Label maxRedCount = new Label("Max: 1");
        redCount.setTextAlignment(TextAlignment.CENTER);
        maxRedCount.setTextAlignment(TextAlignment.CENTER);
        redFrogs.setAlignment(Pos.CENTER);
        redFrogs.getChildren().addAll(redCount, maxRedCount);

        VBox greenFrogs = new VBox();
        greenCount = new Label("Green: 0");
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
        deleteBtn.setDisable(true);
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
        redFrog.setOnDragDetected(e -> {
            Dragboard db1 = redFrog.startDragAndDrop(TransferMode.MOVE);

            // preview img under cursor while dragging
            ImageView btnIcon = (ImageView) redFrog.getGraphic();
            if (btnIcon != null) {
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                db1.setDragView(btnIcon.snapshot(params, null));
            }

            ClipboardContent content = new ClipboardContent();
            content.putString("R");
            db1.setContent(content);
            e.consume();
        });

        greenFrog.setOnAction(e -> {
            placing = true;
            frogType.set("G");
            placementLock(placing);
        });
        greenFrog.setOnDragDetected(e -> {
            Dragboard db2 = greenFrog.startDragAndDrop(TransferMode.MOVE);

            // preview img under cursor while dragging
            ImageView btnIcon = (ImageView) greenFrog.getGraphic();
            if (btnIcon != null) {
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                db2.setDragView(btnIcon.snapshot(params,null));
            }

            ClipboardContent content = new ClipboardContent();
            content.putString("G");
            db2.setContent(content);
            e.consume();
        });

        exitBtn.setOnAction(e -> exitDialog());

        cancelBtn.setOnAction(e -> {
            placing = false;
            placementLock(false);
            displayLabel.setText("Placement canceled");
        });

        saveBtn.setOnAction(e -> model.save());

        deleteBtn.setOnAction(e -> {
            displayLabel.setText("Select a piece to delete off the board");
            deleting = true;
        });

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
    private void placementLock(boolean entered) {
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
    private void updateGrid(GridPane currentBoard) {
        HoppersConfig modelBoard;
        if (currentBoard == customBoard) {
            redCount.setText("Red: " + model.getRedFrogCount());
            greenCount.setText("Green: " + model.getGreenFrogCount());
            if (model.getRedFrogCount() == 0 && model.getGreenFrogCount() == 0) {
                deleteBtn.setDisable(true);
            } else {
                deleteBtn.setDisable(false);
            }
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
    private void chooseFile(Stage stage) {
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

