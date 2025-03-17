import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Driver class to decide if an undirected graph G: (V,E) can be 3-colored.
 */
public class Find3Color {

    /**
     * Represents a graph with an adjacency matrix.
     */
    public static class Graph {
        private ArrayList<ArrayList<Integer>> adjacencyMatrix;
        private int numVertices;
        private ArrayList<Color> vertexColors; // represents each vertex and its color (1, 2, or 3)

        /**
         * Colors that vertices in the graph can be.
         */
        public enum Color {
            Red,
            Blue,
            Green,
        }

        /**
         * Creates a new Graph with this adjacency matrix.
         *
         * @param adjacencyMatrix The adjacency matrix.
         */
        public Graph(ArrayList<ArrayList<Integer>> adjacencyMatrix) {
            this.adjacencyMatrix = adjacencyMatrix;
            this.numVertices = adjacencyMatrix.size();
            this.vertexColors = new ArrayList<>();

            // create colors list
            for (int i = 0; i < adjacencyMatrix.size(); i++) {
                // every vertex starts off red
                this.vertexColors.add(Color.Red);
            }
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
        public void find3Color() {
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: "
                    + " <input_file>\n    where input_file is the input graph file to find a 3 color graph of.");
            System.exit(1);
        }

        // read the graph from the file and save it
        ArrayList<Graph> graphs = readUndirectedGraphsFromFile(args[0]);
    }

    /**
     * Reads a graph from a file and
     * 
     * @param fileName
     * @return
     */
    public static ArrayList<Graph> readUndirectedGraphsFromFile(String fileName) {
        ArrayList<Graph> output = new ArrayList<>();
        File inputFile = new File(fileName);
        try {
            Scanner fileScanner = new Scanner(inputFile);
            while (fileScanner.hasNextLine()) {

                // read the next graph
                String currentLine = fileScanner.nextLine();
                int numVertices = Integer.parseInt(currentLine);

                // break if we read a 0 for the graph number of vertices
                if (numVertices == 0) {
                    break;
                }

                ArrayList<ArrayList<Integer>> currentAdjacencyMatrix = new ArrayList<>();

                // iterate through rows
                for (int i = 0; i < numVertices; i++) {
                    if (!fileScanner.hasNextLine()) {
                        System.err.println(
                                "Error: File does not have expected row in graph " + (output.size() + 1)
                                        + " for vertex " + i);
                        System.exit(1);
                    }

                    currentLine = fileScanner.nextLine();
                    // split this string by spaces so we can see if it is valid or not
                    String curRowValues[] = currentLine.split(" ");

                    if (curRowValues.length != numVertices) {
                        System.err.println(
                                "Error: Invalid number of columns, expected " + numVertices + " but got "
                                        + curRowValues.length);
                        System.exit(1);
                    }

                    // holds the row of the adjacency matrix
                    ArrayList<Integer> currentRow = new ArrayList<>();

                    // iterate through the columns
                    for (int j = 0; j < curRowValues.length; j++) {
                        // try to parse the values
                        try {
                            int currentVal = Integer.parseInt(curRowValues[j]);
                            // push this element of the adjacency matrix to the current row
                            currentRow.add(currentVal);
                        } catch (NumberFormatException nfe) {
                            System.err.println("Error: File does not have expected data at graph " + (output.size() + 1)
                                    + ", row " + i + ", column " + j);
                            System.exit(1);
                        }
                    }

                    // push this row to the adjacency matrix
                    currentAdjacencyMatrix.add(currentRow);
                }

                // add this graph to the output
                output.add(new Graph(currentAdjacencyMatrix));

                // DEBUG: print the array that we created
                System.out.println("DEBUG: array we just pulled");
                for (int i = 0; i < currentAdjacencyMatrix.size(); i++) {
                    for (int j = 0; j < currentAdjacencyMatrix.size(); j++) {
                        System.out.print(currentAdjacencyMatrix.get(i).get(j) + " ");
                    }
                    System.out.println();
                }
            }

            fileScanner.close();
        } catch (Exception e) {
            System.err.println("Error: problem ocurred when reading from file. Is the file properly formatted?");
            System.exit(1);
        }

        return output;
    }
}
