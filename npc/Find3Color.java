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
    public static class UndirectedGraph {
        private final static int PRINT_THRESHOLD = 20;
        private static int nextGraphNum = 1; // start at 1
        private int graphNum;
        private ArrayList<ArrayList<Integer>> adjacencyMatrix;
        private int numVertices;
        private int numEdges;
        private ArrayList<Color> vertexColors; // represents each vertex and its color (1, 2, or 3)

        /**
         * Colors that vertices in the graph can be.
         */
        public enum Color {
            RED,
            BLUE,
            GREEN,
            ;

            /**
             * Converts the enum to a string.
             * 
             * @return The string representation of a Color for printing a Graph.
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
         * Creates a new Graph with this adjacency matrix.
         *
         * @param adjacencyMatrix The adjacency matrix.
         */
        public UndirectedGraph(ArrayList<ArrayList<Integer>> adjacencyMatrix) {
            this.graphNum = UndirectedGraph.nextGraphNum;
            UndirectedGraph.nextGraphNum++; // increment the next graph number
            this.adjacencyMatrix = adjacencyMatrix;
            this.numVertices = adjacencyMatrix.size();
            this.numEdges = countEdges();
            this.vertexColors = new ArrayList<>();

            // create colors list
            for (int i = 0; i < adjacencyMatrix.size(); i++) {
                // every vertex starts off red
                this.vertexColors.add(Color.RED);
            }
        }

        /**
         * Counts the number of edges in the graph.
         * 
         * @return The number of edges in the graph.
         */
        private int countEdges() {
            int numEdgesOut = 0;
            for (int i = 0; i < this.numVertices; i++) {
                // count all edges that are on the top right side of the adjacency matrix, as
                // this graph should be undirected
                for (int j = i + 1; j < this.numVertices; j++) {
                    if (this.adjacencyMatrix.get(i).get(j) > 0) {
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
         */
        private void printGraph(long timeElapsedMillis, boolean is3Colorable) {
            System.out.print(
                    "G" + this.graphNum + ":(|V|=" + this.numVertices + ",|E|=" + this.numEdges + ") ");
            if (this.numVertices >= PRINT_THRESHOLD && is3Colorable) {
                // print out this color plan since we are at or above the print threshold
                String colorPlan = "";
                for (int i = 0; i < this.vertexColors.size(); i++) {
                    colorPlan += vertexColors.get(i) + " "; // the last element will also have a space after it
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
                for (int i = 0; i < this.vertexColors.size(); i++) {
                    colorPlan += vertexColors.get(i) + " "; // the last element will also have a space after it
                }
                System.out.println(colorPlan);

                // iterate through rows
                for (int i = 0; i < this.adjacencyMatrix.size(); i++) {
                    // print out the color first
                    System.out.print(vertexColors.get(i) + " ");

                    // iterate through columns
                    for (int j = 0; j < this.adjacencyMatrix.size(); j++) {
                        if (i == j) {
                            System.out.print("X ");
                        } else {
                            System.out.print(this.adjacencyMatrix.get(i).get(j) + " ");
                        }
                    }
                    System.out.println();
                }
            }
        }

        /**
         * Checks that 3 color is satisfied for this Graph.
         * 3 Color is satisfied when no two vertices of the same color are adjacent to
         * one another.
         * 
         * @return True if 3 Color is satisfied, false otherwise.
         */
        public boolean is3ColorSatisfied() {
            boolean isSatisfied = true; // satisfied until proven unsatisfied

            for (int i = 0; i < this.numVertices; i++) {
                // let's check from the top-right of the adjacency matrix, as we are in an
                // undirected graph
                for (int j = i + 1; j < this.numVertices; j++) {
                    if (this.adjacencyMatrix.get(i).get(j) > 0
                            && this.vertexColors.get(i) == this.vertexColors.get(j)) {
                        // this graph does not satisfy 3 color if two adjacent vertices are the same
                        // color
                        isSatisfied = false;
                        break;
                    }
                }

                // break if not satisfied
                if (!isSatisfied) {
                    break;
                }
            }

            return isSatisfied;
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
        public boolean convertTo3Color() {
            return convertTo3ColorHelper(0);
        }

        private boolean convertTo3ColorHelper(int vertexIndex) {
            // base case: we have assigned a color to every vertex in the graph
            if (vertexIndex == this.numVertices) {
                // see if this assignment satisfies 3Color
                return is3ColorSatisfied();
            }

            // try setting this vertex to be green
            this.vertexColors.set(vertexIndex, Color.GREEN);
            if (convertTo3ColorHelper(vertexIndex + 1)) { // recurse
                // we found a solution
                return true;
            }

            // try setting this vertex to be blue
            this.vertexColors.set(vertexIndex, Color.BLUE);
            if (convertTo3ColorHelper(vertexIndex + 1)) { // recurse
                // we found a solution
                return true;
            }

            // try setting this vertex to be green
            this.vertexColors.set(vertexIndex, Color.RED);
            if (convertTo3ColorHelper(vertexIndex + 1)) { // recurse
                // we found a solution
                return true;
            }

            // if none of these assignments work from this point, we backtrack
            return false;
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: "
                    + " <input_file>\n    where input_file is the input graph file to find a 3 color graph of.");
            System.exit(1);
        }

        // read the graph from the file and save it
        ArrayList<UndirectedGraph> graphs = readUndirectedGraphsFromFile(args[0]);

        for (int i = 0; i < graphs.size(); i++) {
            UndirectedGraph curGraph = graphs.get(i);
            long startTime = System.currentTimeMillis();
            boolean is3Colorable = curGraph.convertTo3Color();
            long elapsedTime = System.currentTimeMillis() - startTime;
            curGraph.printGraph(elapsedTime, is3Colorable);
        }
    }

    /**
     * Reads a graph from a file and
     * 
     * @param fileName
     * @return
     */
    public static ArrayList<UndirectedGraph> readUndirectedGraphsFromFile(String fileName) {
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
