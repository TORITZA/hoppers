package core.common.solver;

import java.util.*;

/**
 * A simple puzzle solver used to find the shortest path to a given solution,
 * implementing BFS in tandem with backtracking to achieve this.
 *
 * @author Tereza Lang (@TORITZA)
 */
public class Solver {
    /** TOTAL number of configurations generated upon solving */
    private int totalConfigs;
    /** number of DISTINCT configurations generated */
    private int uniqueConfigs;

    /**
     * Creates a new core.Solver object with totalConfigs & uniqueConfigs initialized
     * to zero.
     */
    public Solver() {
        this.totalConfigs = 0;
        this.uniqueConfigs = 0;
    }

    /**
     * A getter for the core.Solver's current amount of total configurations.
     *
     * @return total number of configurations generated
     */
    public int getTotalConfigs() {
        return totalConfigs;
    }

    /**
     * A getter for the number of unique configurations produced
     * by the core.Solver.
     *
     * @return total number of unique configurations created
     */
    public int getUniqueConfigs() {
        return uniqueConfigs;
    }

    /**
     * The main BFS + backtracking solving algorithm, used to find a solution
     * and the shortest path to it.
     * Employs a helper method to construct & return the resulting path.
     *
     * @param config the initial puzzle configuration
     * @return the path traversed to get to a defined solution
     */
    public Collection<Configuration> solve(Configuration config) {
        // initialize queue
        List<Configuration> q = new LinkedList<>();
        // add initial configuration
        q.add(config);

        // create predecessor map
        Map<Configuration, Configuration> pred = new HashMap<>();
        pred.put(config, config);

        Configuration curr = null;
        // enter main BFS algorithm loop
        while (!q.isEmpty()) {
            curr = q.removeFirst();
            if (curr.isSolution()) {
                totalConfigs++;
                break;
            }
            for (Configuration ngh : curr.getNeighbors()) {
                // process unvisited neighbors
                if (!pred.containsKey(ngh)) {
                    pred.put(ngh, curr);
                    q.add(ngh);
                }
                totalConfigs++;
            }
        }
        // pred map ensures uniqueness!
        uniqueConfigs += pred.size();

        if (curr == null || !curr.isSolution()) {
            return null;
        }

        return pathFound(pred, config, curr);
    }

    /**
     * A helper function used to construct the path to a solution based on a provided
     * predecessor map, initial configuration, and ending configuration.
     *
     * @param predMap a HashMap containing a config and the parent config it was derived from
     * @param initial the initial, starting config of the puzzle
     * @param end the solution
     * @return the path traversed to get to the defined solution
     */
    private Collection<Configuration> pathFound(Map<Configuration, Configuration> predMap,
                                                Configuration initial, Configuration end) {

        List<Configuration> path = new LinkedList<>();

        if (predMap.containsKey(end)) {
            Configuration curr = end;
            while (!curr.equals(initial)) {
                path.addFirst(curr);
                curr = predMap.get(curr);
            }
            path.addFirst(initial);
        }
        return path;
    }

}

