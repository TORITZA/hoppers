package core.hoppers.solver;

import core.common.solver.Configuration;
import core.common.solver.Solver;
import core.hoppers.model.HoppersConfig;

import java.io.IOException;
import java.util.Collection;

/**
 * Main class for the Hoppers puzzle.
 *
 * @author Tereza Lang (@TORITZA), RIT CS
 */
public class Hoppers {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.out.println("Usage: java Hoppers filename");

        } else {

            HoppersConfig inital = new HoppersConfig(args[0]);
            Solver sol = new Solver();
            Collection<Configuration> steps = sol.solve(inital);

            // beginning of main output display:
            System.out.println("File: " + args[0]);
            System.out.println(inital);
            System.out.println("Total configs: " + sol.getTotalConfigs());
            System.out.println("Unique configs: " + sol.getUniqueConfigs());

            if (steps == null) {
                System.out.println("No solution found");
            } else {
                int i = 0;
                for (Configuration config : steps) {
                    System.out.println("Step " + i + ": " + "\n" + config + "\n");
                    i++;
                }
            }
        }
    }
}

