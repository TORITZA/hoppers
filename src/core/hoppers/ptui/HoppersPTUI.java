package core.hoppers.ptui;

import core.common.Observer;
import core.hoppers.model.HoppersModel;

import java.io.IOException;
import java.util.Scanner;

/**
 * The plain text for the Hoppers puzzle. This class encapsulates both
 * the View and Controller portions of the MVC architecture.
 *
 * @author Tereza Lang (@TORITZA), RIT CS
 */
public class HoppersPTUI implements Observer<HoppersModel, String> {
    private HoppersModel model;

    /**
     * The View initialization, adding it as an observer of the Hoppers model.
     *
     * @param filename the file whose contents are to be configured into a
     *      *                 Hoppers puzzle
     * @throws IOException thrown when file cannot be found
     */
    public void init(String filename) throws IOException {
        this.model = new HoppersModel(filename);
        this.model.addObserver(this);
        displayHelp();
    }

    /**
     * Updates the PTUI by printing the current iteration of the game board
     * and the operation in response to the user's command.
     *
     * @param model informs the view that the internal logic has changed
     *              and the external display should reflect that
     * @param data the message the model sends to this observer
     *
     */
    @Override
    public void update(HoppersModel model, String data) {
        System.out.println(data);
        System.out.println(model);
    }

    /**
     * Displays in standard output the list of available commands.
     */
    private void displayHelp() {
        System.out.println( "h(int)              -- hint next move" );
        System.out.println( "l(oad) filename     -- load new puzzle file" );
        System.out.println( "s(elect) r c        -- select cell at r, c" );
        System.out.println( "q(uit)              -- quit the game" );
        System.out.println( "r(eset)             -- reset the current game" );
    }

    /*
     ******************* THE CONTROLLER *********************************
     */

    /**
     * A loop that prompts for user input and makes calls into the HoppersModel.
     */
    public void run() {
        Scanner in = new Scanner( System.in );
        for ( ; ; ) {
            System.out.print( "> " );
            String line = in.nextLine();
            String[] words = line.split( "\\s+" );
            if (words.length > 0) {
                if (words[0].startsWith("q")) {
                    break;
                } else if (words[0].startsWith("h")) {
                    // display next config in solver's returned path
                    model.hint();
                } else if (words[0].startsWith("l")) {
                    // call to model to load puzzle file
                    try {
                        model.load(words[1]);
                    } catch (ArrayIndexOutOfBoundsException a) {
                        System.out.println("ERROR: Please provide a file name.");
                    }
                } else if (words[0].startsWith("s")) {
                    // have model select internal cell using other two arguments (words[1] & [2])
                    // wait for next selection
                    try {
                        model.select(words[1], words[2]);
                    } catch (ArrayIndexOutOfBoundsException a) {
                        System.out.println("ERROR: Please provide a row and/or column.");
                    }
                } else if (words[0].startsWith("r")) {
                    model.reset();
                // else if (...) Creation Mode
                } else {
                    displayHelp();
                }
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java HoppersPTUI filename");
        } else {
            try {
                HoppersPTUI ptui = new HoppersPTUI();
                ptui.init(args[0]);
                ptui.run();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }
}

