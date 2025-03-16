import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Driver class to decide if an undirected graph G: (V,E) can be 3-colored.
 * 
 * TODO: use an adjacency matrix or adjacency list to represent the graph
 */
public class Find3Color {

    /**
     * Represents a graph with an adjacency matrix.
     */
    public class Graph {
        private ArrayList<ArrayList<Integer>> adjacencyMatrix;
        private int numVertices;
        private int numEdges;

        /**
         * Creates a new Graph with this adjacency matrix.
         *
         * @param adjacencyMatrix The adjacency matrix.
         */
        public Graph(ArrayList<ArrayList<Integer>> adjacencyMatrix) {
            this.adjacencyMatrix = adjacencyMatrix;
        }

        /**
         * Creates a new Graph from an adjacency matrix string.
         * 
         * @param adjacencyMatrixString
         */
        public Graph(String adjacencyMatrixString) {
            
        }

        /**
         * FIXME: It is likely that we want to rename this function but the general idea
         * is to do a modified depth-first search where we will visit every node and
         * color one, and if the node that we color with our current color touches
         * another of the same color, then we will backtrack on this node (or even back
         * to a previous node) to change the color of the node to its next value. I
         * believe
         * that the only point where we know that we cannot do a 3-color with just
         * backtracking is if we go through the entire algorithm and do not find a
         * solution, which means that this would take a long time to find a
         * non-solution. There are likely better ways to find out that we a graph cannot
         * be 3-colored, but that is probably not the scope of this assignment.
         */
        public void depthFirstSearch() {

        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: "
                    + " <input_file>\n    where input_file is the input graph file to find a 3 color graph of.");
            System.exit(1);
        }

        // read the graph from the file and save it
        ArrayList<Graph> graphs = readGraphFromFile(args[0]);
    }

    /**
     * Reads a graph from a file and
     * 
     * @param fileName
     * @return
     */
    public static ArrayList<Graph> readGraphFromFile(String fileName) {
        ArrayList<Graph> output = new ArrayList<>();
        File inputFile = new File(fileName);
        try {
            Scanner fileScanner = new Scanner(inputFile);
            while (fileScanner.hasNextLine()) {
                System.out.println(fileScanner.nextLine());
            }

            fileScanner.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return output;
    }
}
