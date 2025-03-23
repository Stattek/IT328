
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Random;

/**
 * Class to hold functionality for deciding if p is an elemeent of 3NAESAT by
 * reducing it to a three-color problem.
 */
public class find3NAESAT3Color {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(
                    "Usage: java Find3NAESAT <input_file>\n    where input_file is the input file to read 3CNFs from.");
            System.exit(1);
        }

        String fileName = args[0];
        File file = new File(fileName);

        Random random = new Random();

        System.out.println("** Find 3NAESAT in " + fileName + " (reduced to 3-Color Problem):\n");

        // Generating an ArrayList of all CNFFormulas from the file.
        try {
            ArrayList<CNFFormula> formulas = parseFile(file);

            int formulaCounter = 0;
            for (CNFFormula formula : formulas) {
                formulaCounter++;
                long start = System.currentTimeMillis();

                int[][] adjacencyMatrix = convertToAdjacencyMatrix(formula);
                UndirectedGraph graph = new UndirectedGraph(adjacencyMatrix);
                boolean is3Colorable = graph.isConvertableTo3Color();
                long end = System.currentTimeMillis();
                // Duration of the operation
                long duration = end - start;

                // Temporary string to help with formatting
                String temp = "";

                // Calculate the number of vertices and edges
                int numVertices = adjacencyMatrix.length;
                int numEdges = graph.countEdges();

                // First line output formatting
                System.out.println("3CNF No." + formulaCounter + ":[n=" + formula.getNumLiterals() + " k="
                        + formula.getNumClauses() + "] -> [V=" + numVertices + ", E=" + numEdges + "]");

                boolean[] assignment = new boolean[formula.getNumLiterals() + 1];
                formula.setAssignment(assignment);
                // Second line output formatting, dependent upon if the current CNFFormula is
                // 3NAESAT
                if (is3Colorable) {
                    System.out.print("(" + duration + " ms) NAE certificate = [");

                    // Determine the true color
                    UndirectedGraph.Color trueColor = graph.vertexColors[1];

                    // Assign truth values to literals based on their colors
                    for (int i = 1; i <= formula.getNumLiterals(); i++) {
                        UndirectedGraph.Color literalColor = graph.vertexColors[i]; // Literal color

                        if (literalColor == trueColor) {
                            formula.setAssignment(i, true); // Literal is true
                        } else {
                            formula.setAssignment(i, false); // Literal is false
                        }
                    }
                } else {
                    System.out.print("(" + duration + " ms) No NAE positive certificate!  Using random assignment = [");
                    for (int i = 1; i <= formula.getNumLiterals(); i++) {
                        formula.setAssignment(i, random.nextBoolean());
                    }
                }

                for (int i = 1; i <= formula.getNumLiterals(); i++) {
                    String boolVal;
                    if (formula.getAssignment(i)) {
                        boolVal = "T";
                    } else {
                        boolVal = "F";
                    }

                    temp += (i + ":" + boolVal + " ");
                }

                System.out.println(temp.trim() + "]");

                // Third line output formatting, individual clauses and their literals
                temp = "";
                for (Clause clause : formula.getClauses()) {
                    for (int i = 0; i <= 2; i++) {
                        if (i == 0) {
                            temp += "(";
                        } else {
                            temp += "|";
                        }

                        if (clause.getLiteral(i) < 0) {
                            temp += clause.getLiteral(i);
                        } else {
                            temp += (" " + clause.getLiteral(i));
                        }

                        if (i == 2) {
                            temp += ")";
                        }
                    }

                    temp += "^";
                }

                temp = temp.substring(0, temp.length() - 1);
                System.out.println(temp + " ==>");

                // Fourth line output formatting, literal boolean value; based on their
                // assignment and if the given literal is a negation of the assignment.
                temp = "";
                for (Clause clause : formula.getClauses()) {
                    for (int i = 0; i <= 2; i++) {
                        if (i == 0) {
                            temp += "(";
                        } else {
                            temp += "|";
                        }

                        int literal = clause.getLiteral(i);
                        int index = Math.abs(literal);
                        boolean assigned = formula.getAssignment(index);

                        if (literal < 0) {
                            assigned = !assigned;
                        }

                        if (assigned) {
                            temp += " T";
                        } else {
                            temp += " F";
                        }

                        if (i == 2) {
                            temp += ")";
                        }
                    }

                    temp += "^";
                }
                temp = temp.substring(0, temp.length() - 1);
                System.out.println(temp + "\n");
            }
        } catch (FileNotFoundException fnfe) {
            fnfe.printStackTrace();
        }

    }

    /**
     * Converts a 3-CNF formula into an adjacency matrix for the 3-color
     * problem.
     *
     * @param formula The CNFFormula to convert.
     * @return The adjacency matrix representing the graph.
     */
    public static int[][] convertToAdjacencyMatrix(CNFFormula formula) {
        int numLiterals = formula.getNumLiterals();
        int numClauses = formula.getNumClauses();

        // Total vertices = 1 base node + literals (positive and negative) + 3 nodes per
        // clause
        int totalVertices = 1 + (2 * numLiterals) + (3 * numClauses);

        // Initialize adjacency matrix without any edges
        int[][] adjacencyMatrix = new int[totalVertices][totalVertices];
        for (int i = 0; i < totalVertices; i++) {
            for (int j = 0; j < totalVertices; j++) {
                adjacencyMatrix[i][j] = 0;
            }
        }

        // Base node index
        int baseNode = 0;

        // Add edges between base node and literals/negations
        for (int i = 0; i < numLiterals; i++) {
            int literalVertex = 1 + i; // Literal node index
            int negLiteralVertex = 1 + numLiterals + i; // Negation node index

            // Connect base node to literal and negation
            adjacencyMatrix[baseNode][literalVertex] = 1;
            adjacencyMatrix[literalVertex][baseNode] = 1;

            adjacencyMatrix[baseNode][negLiteralVertex] = 1;
            adjacencyMatrix[negLiteralVertex][baseNode] = 1;

            // Connect literal to its negation
            adjacencyMatrix[literalVertex][negLiteralVertex] = 1;
            adjacencyMatrix[negLiteralVertex][literalVertex] = 1;
        }

        // Clause nodes: Add edges for each clause
        ArrayList<Clause> clauses = formula.getClauses();
        for (int clauseIndex = 0; clauseIndex < clauses.size(); clauseIndex++) {
            Clause clause = clauses.get(clauseIndex);

            // Clause node indices
            int clauseNode1 = 1 + (2 * numLiterals) + (3 * clauseIndex);
            int clauseNode2 = clauseNode1 + 1;
            int clauseNode3 = clauseNode1 + 2;

            // Connect clause nodes to form a triangle
            adjacencyMatrix[clauseNode1][clauseNode2] = 1;
            adjacencyMatrix[clauseNode2][clauseNode1] = 1;

            adjacencyMatrix[clauseNode2][clauseNode3] = 1;
            adjacencyMatrix[clauseNode3][clauseNode2] = 1;

            adjacencyMatrix[clauseNode3][clauseNode1] = 1;
            adjacencyMatrix[clauseNode1][clauseNode3] = 1;

            // Connect clause nodes to corresponding literals/negations in Group A
            for (int i = 0; i < 3; i++) {
                int literal = clause.getLiteral(i);
                int literalVertex;

                if (literal > 0) {
                    literalVertex = 1 + (literal - 1); // Positive literal
                } else {
                    literalVertex = 1 + numLiterals + (Math.abs(literal) - 1); // Negation
                }

                // Connect clause node to corresponding literal/negation
                int clauseNode = clauseNode1 + i; // Current clause node
                adjacencyMatrix[clauseNode][literalVertex] = 1;
                adjacencyMatrix[literalVertex][clauseNode] = 1;
            }
        }

        return adjacencyMatrix;
    }

    private static class UndirectedGraph {
        private final static int PRINT_THRESHOLD = 20;
        private static int nextGraphNum = 1; // start at 1
        private int graphNum;
        private int[][] adjacencyMatrix;
        private int numVertices;
        private int numEdges;
        public Color[] vertexColors; // represents each vertex and its color

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

    /**
     * Takes input from a file, and using the generateCNFFormula method, returns
     * an ArrayList of CNFFormula instances.
     *
     * @param file - File Object for the given input file
     * @return An ArrayList of CNFFormula instances
     * @throws FileNotFoundException
     */
    static ArrayList<CNFFormula> parseFile(File file) throws FileNotFoundException {
        ArrayList<CNFFormula> formulas = new ArrayList<>();

        try (Scanner fileScanner = new Scanner(file)) {
            while (fileScanner.hasNextLine()) {
                CNFFormula curFormula = generateCNFFormula(fileScanner);
                formulas.add(curFormula);
            }
        }

        return formulas;
    }

    /**
     * Parses a single line in the input file and converts it to a CNFFormula
     * object.
     *
     * @param fileScanner - Scanner object for the input file
     * @return A CNFFormula instance for the given line in the file
     */
    static CNFFormula generateCNFFormula(Scanner fileScanner) {
        CNFFormula currentCNFFormula = new CNFFormula();
        String line = fileScanner.nextLine();
        Scanner lineScanner = new Scanner(line);

        while (lineScanner.hasNextInt()) {
            int[] clauseLiterals = new int[3];

            for (int i = 0; i < 3; i++) {
                int literal = lineScanner.nextInt();

                clauseLiterals[i] = literal;

                if (Math.abs(literal) > currentCNFFormula.getNumLiterals()) {
                    currentCNFFormula.setNumLiterals(Math.abs(literal));
                }
            }

            currentCNFFormula.addClause(new Clause(clauseLiterals));
        }
        lineScanner.close();
        return currentCNFFormula;
    }

    /**
     * Clause Class
     *
     * Represents a single clause within the CNFFormula.
     *
     * @author Andrew Ott
     */
    public static class Clause {

        private int[] literals;

        Clause(int[] literals) {
            this.literals = literals;
        }

        public int getLiteral(int index) {
            return literals[index];
        }

        /**
         * Clause isNAESatisfied method
         *
         * checks to see that the given clause is satisfied.
         *
         * @param assignment
         * @return True if the clause satisfies NAESAT, false otherwise
         */
        public boolean isNAESatisfied(boolean[] assignment) {
            boolean hasTrue = false;
            boolean hasFalse = false;

            for (int i = 0; i < literals.length; i++) {
                int lit = getLiteral(i);
                int index = Math.abs(lit);
                boolean value = assignment[index];

                // If literal is negative, switch the boolean
                if (lit < 0) {
                    value = !value;
                }

                if (value) {
                    hasTrue = true;
                } else {
                    hasFalse = true;
                }

                // Early termination if the clause has both true and false
                if (hasTrue && hasFalse) {
                    return true;
                }
            }

            return hasTrue && hasFalse;
        }

    }

    /**
     * CNFFormula class
     *
     * Represents a given CNFFormula; it's number of clauses and literals, the
     * assignment of each literal, as well as the individual clauses.
     *
     * @author Andrew Ott
     */
    public static class CNFFormula {

        private int numLiterals;
        private int numClauses;
        // Holds the boolean value for each literal
        private boolean[] assignment;
        // Array list of each clause
        private ArrayList<Clause> clauses;

        CNFFormula() {
            this.numLiterals = 0;
            this.numClauses = 0;

            clauses = new ArrayList<Clause>();
        }

        void addClause(Clause newClause) {
            clauses.add(newClause);
            numClauses++;
        }

        public int getNumLiterals() {
            return numLiterals;
        }

        public void setNumLiterals(int numLiterals) {
            this.numLiterals = numLiterals;
        }

        public int getNumClauses() {
            return numClauses;
        }

        public ArrayList<Clause> getClauses() {
            return clauses;
        }

        public void setAssignment(boolean[] assignment) {
            this.assignment = assignment;
        }

        public void setAssignment(int varIndex, boolean value) {
            assignment[varIndex] = value;
        }

        public boolean getAssignment(int varIndex) {
            return assignment[varIndex];
        }

        /**
         * CNFFormula isNAESatisfied method
         *
         * Calls the Clause isNAESatisfied method on all clauses in the clauses
         * ArrayList.
         *
         * @return True if all clauses are NAESAT, false otherwise.
         */
        public boolean isNAESatisfied() {
            for (Clause clause : clauses) {
                if (!clause.isNAESatisfied(assignment)) {
                    return false;
                }
            }
            return true;
        }
    }
}
