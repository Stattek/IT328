
/**
 * Author: David Slay
 * Summary: Program to find out if undirected graphs can be 3-colored.
 */
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Driver class to decide if an undirected graph G: (V,E) can be 3-colored.
 * 
 * @author David Slay
 */
public class Find3Color {

    /**
     * Represents an undirected graph with an adjacency matrix.
     * 
     * @author David Slay
     */
    private static class UndirectedGraph {
        private final static int PRINT_THRESHOLD = 20;
        private static int nextGraphNum = 1; // start at 1
        private int graphNum;
        private int[][] adjacencyMatrix;
        private int numVertices;
        private int numEdges;
        private Color[] vertexColors; // represents each vertex and its color

        /**
         * Colors that vertices in the graph can be.
         * 
         * @author David Slay
         */
        public enum Color {
            RED,
            BLUE,
            GREEN,
            NONE,
            ;

            /**
             * Converts the enum to a string.
             * 
             * @return The string representation of a Color for printing an UndirectedGraph.
             * @author David Slay
             */
            @Override
            public String toString() {
                switch (this) {
                    case RED:
                        return "R";
                    case BLUE:
                        return "B";
                    case GREEN:
                        return "G";
                    default:
                        // this should never happen
                        return "N"; // NULL color
                }
            }
        }

        /**
         * Creates a new UndirectedGraph with this adjacency matrix.
         *
         * @param adjacencyMatrix The adjacency matrix.
         * @author David Slay
         */
        public UndirectedGraph(int[][] adjacencyMatrix) {
            this.graphNum = UndirectedGraph.nextGraphNum;
            UndirectedGraph.nextGraphNum++; // increment the next graph number
            this.adjacencyMatrix = adjacencyMatrix;
            this.numVertices = adjacencyMatrix.length;
            this.numEdges = countEdges();
            this.vertexColors = new Color[this.numVertices];

            // initialize colors array
            for (int i = 0; i < adjacencyMatrix.length; i++) {
                // every vertex starts off with no color
                this.vertexColors[i] = Color.NONE;
            }
        }

        /**
         * Counts the number of edges in the graph.
         * 
         * @return The number of edges in the graph.
         * @author David Slay
         */
        private int countEdges() {
            int numEdgesOut = 0;
            for (int i = 0; i < this.numVertices; i++) {
                // count all edges that are on the top right side of the adjacency matrix, as
                // this graph should be undirected
                for (int j = i + 1; j < this.numVertices; j++) {
                    if (this.adjacencyMatrix[i][j] > 0) {
                        numEdgesOut++;
                    }
                }
            }

            return numEdgesOut;
        }

        /**
         * Prints a graph with its information.
         * 
         * @param timeElapsedMillis The time elapsed to generate a 3-color graph.
         * @param is3Colorable      If the graph is 3Colorable.
         * @author David Slay
         */
        private void printGraph(long timeElapsedMillis, boolean is3Colorable) {
            System.out.print(
                    "G" + this.graphNum + ":(|V|=" + this.numVertices + ",|E|=" + this.numEdges + ") ");
            if (this.numVertices >= PRINT_THRESHOLD && is3Colorable) {
                // print out this color plan since we are at or above the print threshold
                String colorPlan = "";
                for (int i = 0; i < this.vertexColors.length; i++) {
                    colorPlan += vertexColors[i] + " "; // the last element will also have a space after it
                }
                System.out.print(colorPlan);
            } else if (!is3Colorable) {
                // if this graph is not 3 colorable, print that out
                System.out.print("Not 3-colorable ");
            }
            System.out.println("(ms=" + timeElapsedMillis + ")");

            // only print the rest of this if we are not at or above the print threshold
            if (this.numVertices < PRINT_THRESHOLD && is3Colorable) {
                String colorPlan = "  ";
                for (int i = 0; i < this.vertexColors.length; i++) {
                    colorPlan += vertexColors[i];

                    // add space
                    if (i != this.vertexColors.length - 1) {
                        colorPlan += " ";
                    }
                }
                System.out.println(colorPlan);

                // iterate through rows
                for (int i = 0; i < this.adjacencyMatrix.length; i++) {
                    // print out the color first
                    System.out.print(vertexColors[i] + " ");

                    // iterate through columns
                    for (int j = 0; j < this.adjacencyMatrix.length; j++) {
                        if (i == j) {
                            System.out.print("X");
                        } else if (this.adjacencyMatrix[i][j] != 0) {
                            System.out.print(this.adjacencyMatrix[i][j]);
                        } else {
                            System.out.print(" ");
                        }

                        if (j != this.adjacencyMatrix.length - 1) {
                            System.out.print(" ");
                        }
                    }
                    System.out.println();
                }
            }
        }

        /**
         * Checks if the vertex at this index is adjacent to another vertex with the
         * same color.
         * 
         * @param vertexIndex The index of the vertex to check.
         * @return True if this vertex is adjacent to another vertex of the same color,
         *         false otherwise.
         * @author David Slay
         */
        public boolean isAdjacentToSameColor(int vertexIndex) {
            boolean output = false; // not adjacent

            for (int i = 0; i < this.numVertices; i++) {
                if (i != vertexIndex && this.adjacencyMatrix[vertexIndex][i] > 0
                        && this.vertexColors[vertexIndex] == this.vertexColors[i]) {
                    // adjacent to the same color
                    output = true;
                    break;
                }
            }

            return output;
        }

        /**
         * Finds if there is a solution to the 3 Color problem with this
         * UndirectedGraph. This is done by finding some solution where each vertex in
         * the graph is assigned one of three colors (red, blue, and green), and no two
         * vertices with the same color are allowed to be adjacent to one another.
         * 
         * @return True if the graph is 3 colorable, false otherwise.
         * @author David Slay
         */
        public boolean isConvertableTo3Color() {
            return isConvertableTo3ColorHelper(0);
        }

        /**
         * Helper function for finding solution to 3 Color problem.
         * 
         * @param vertexIndex The current vertex index into the vertexColors ArrayList.
         * @return True if the graph is 3 colorable, false otherwise.
         * @author David Slay
         */
        private boolean isConvertableTo3ColorHelper(int vertexIndex) {
            // base case: we have assigned a color to every vertex in the graph
            if (vertexIndex == this.numVertices) {
                // we have found an assignment for 3 color
                return true;
            }

            // try setting this vertex to be green
            this.vertexColors[vertexIndex] = Color.GREEN;
            if (!isAdjacentToSameColor(vertexIndex) && isConvertableTo3ColorHelper(vertexIndex + 1)) {
                return true;
            }

            // try setting this vertex to be blue
            this.vertexColors[vertexIndex] = Color.BLUE;
            if (!isAdjacentToSameColor(vertexIndex) && isConvertableTo3ColorHelper(vertexIndex + 1)) {
                return true;
            }

            // try setting this vertex to be green
            this.vertexColors[vertexIndex] = Color.RED;
            if (!isAdjacentToSameColor(vertexIndex) && isConvertableTo3ColorHelper(vertexIndex + 1)) {
                return true;
            }

            // if none of these assignments work from this point, we backtrack
            this.vertexColors[vertexIndex] = Color.NONE;
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(
                    "Usage: java Find3Color <input_file>\n    where `input_file` is the input file to read 3CNFs from.");

            System.exit(1);
        }

        String fileName = args[0];
        System.out.println("** Find 3-Color plans for graphs in " + fileName + "\n");

        // read the graph from the file and save it
        ArrayList<UndirectedGraph> graphs = readUndirectedGraphsFromFile(fileName);

        for (int i = 0; i < graphs.size(); i++) {
            // see if all graphs are able to be 3 colorableargs
            UndirectedGraph curGraph = graphs.get(i);
            long startTime = System.currentTimeMillis();
            boolean is3Colorable = curGraph.isConvertableTo3Color();
            long elapsedTime = System.currentTimeMillis() - startTime;
            curGraph.printGraph(elapsedTime, is3Colorable);
        }
    }

    /**
     * Reads graphs from file and saves them as UndirectedGraph objects.
     * 
     * @param fileName The name of the file to read from.
     * @return The ArrayList of UndirectedGraph objects read from file.
     * @author David Slay
     */
    private static ArrayList<UndirectedGraph> readUndirectedGraphsFromFile(String fileName) {
        ArrayList<UndirectedGraph> output = new ArrayList<>();
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

                int[][] currentAdjacencyMatrix = new int[numVertices][numVertices];

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

                    // iterate through the columns
                    for (int j = 0; j < curRowValues.length; j++) {
                        // try to parse the values
                        try {
                            int currentVal = Integer.parseInt(curRowValues[j]);
                            if (currentVal != 0 || currentVal != 1) {
                                System.err.println(
                                        "Error: Invalid integer value at graph (expected 1 or 0 but got " + currentVal
                                                + ") " + (output.size() + 1)
                                                + ", row " + i + ", column " + j);
                                System.exit(1);
                            }

                            // add this element
                            currentAdjacencyMatrix[i][j] = currentVal;
                        } catch (NumberFormatException nfe) {
                            System.err.println("Error: File does not have expected data at graph " + (output.size() + 1)
                                    + ", row " + i + ", column " + j);
                            System.exit(1);
                        }
                    }
                }

                // add this graph to the output
                output.add(new UndirectedGraph(currentAdjacencyMatrix));
            }

            fileScanner.close();
        } catch (Exception e) {
            System.err.println(
                    "Error: problem ocurred when reading from file. Does the file exist or is it properly formatted?");
            System.exit(1);
        }

        return output;
    }
}
