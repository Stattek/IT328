
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
public class Find3NAESAT3Color {

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

                ArrayList<ArrayList<Integer>> adjacencyMatrix = convertToAdjacencyMatrix(formula);
                UndirectedGraph graph = new UndirectedGraph(adjacencyMatrix);
                boolean is3Colorable = graph.isConvertableTo3Color();
                long end = System.currentTimeMillis();
                // Duration of the operation
                long duration = end - start;

                // Temporary string to help with formatting
                String temp = "";

                // Calculate the number of vertices and edges
                int numVertices = adjacencyMatrix.size();
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
                    // Get the color of the base node (X)
                    UndirectedGraph.Color baseColor = graph.vertexColors.get(0);

                    // Determine the true color
                    UndirectedGraph.Color trueColor = graph.vertexColors.get(1);

                    // Assign truth values to literals based on their colors
                    for (int i = 1; i <= formula.getNumLiterals(); i++) {
                        UndirectedGraph.Color literalColor = graph.vertexColors.get(i); // Literal color

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
    public static ArrayList<ArrayList<Integer>> convertToAdjacencyMatrix(CNFFormula formula) {
        int numLiterals = formula.getNumLiterals();
        int numClauses = formula.getNumClauses();

        // Total vertices = 1 base node + literals (positive and negative) + 3 nodes per clause
        int totalVertices = 1 + (2 * numLiterals) + (3 * numClauses);

        // Initialize adjacency matrix without any edges
        ArrayList<ArrayList<Integer>> adjacencyMatrix = new ArrayList<>();
        for (int i = 0; i < totalVertices; i++) {
            ArrayList<Integer> row = new ArrayList<>();
            for (int j = 0; j < totalVertices; j++) {
                row.add(0);
            }
            adjacencyMatrix.add(row);
        }

        // Base node index
        int baseNode = 0;

        //Add edges between base node and literals/negations
        for (int i = 0; i < numLiterals; i++) {
            int literalVertex = 1 + i; // Literal node index
            int negLiteralVertex = 1 + numLiterals + i; // Negation node index

            // Connect base node to literal and negation
            adjacencyMatrix.get(baseNode).set(literalVertex, 1);
            adjacencyMatrix.get(literalVertex).set(baseNode, 1);

            adjacencyMatrix.get(baseNode).set(negLiteralVertex, 1);
            adjacencyMatrix.get(negLiteralVertex).set(baseNode, 1);

            // Connect literal to its negation
            adjacencyMatrix.get(literalVertex).set(negLiteralVertex, 1);
            adjacencyMatrix.get(negLiteralVertex).set(literalVertex, 1);
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
            adjacencyMatrix.get(clauseNode1).set(clauseNode2, 1);
            adjacencyMatrix.get(clauseNode2).set(clauseNode1, 1);

            adjacencyMatrix.get(clauseNode2).set(clauseNode3, 1);
            adjacencyMatrix.get(clauseNode3).set(clauseNode2, 1);

            adjacencyMatrix.get(clauseNode3).set(clauseNode1, 1);
            adjacencyMatrix.get(clauseNode1).set(clauseNode3, 1);

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
                adjacencyMatrix.get(clauseNode).set(literalVertex, 1);
                adjacencyMatrix.get(literalVertex).set(clauseNode, 1);
            }
        }

        return adjacencyMatrix;
    }


    /*---------------------------------------------------------------------------------------------------------------------------------------- */

    /*
     * 
     */
    public static class UndirectedGraph {

        private final static int PRINT_THRESHOLD = 20;
        private static int nextGraphNum = 1; // start at 1
        private int graphNum;
        private ArrayList<ArrayList<Integer>> adjacencyMatrix;
        private int numVertices;
        private int numEdges;
        public ArrayList<Color> vertexColors; // represents each vertex and its color (1, 2, or 3)

        /**
         * Colors that vertices in the graph can be.
         *
         * @author David Slay
         */
        public enum Color {
            RED,
            BLUE,
            GREEN,;

            /**
             * Converts the enum to a string.
             *
             * @return The string representation of a Color for printing an
             * UndirectedGraph.
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
         * @author David Slay
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
         * @param timeElapsedMillis The time elapsed to generate a 3-color
         * graph.
         * @param is3Colorable If the graph is 3Colorable.
         * @author David Slay
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
                    colorPlan += vertexColors.get(i);

                    // add space
                    if (i != this.vertexColors.size() - 1) {
                        colorPlan += " ";
                    }
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
         * Checks that 3 color is satisfied for this UndirectedGraph. 3 Color is
         * satisfied when no two vertices of the same color are adjacent to one
         * another.
         *
         * @return True if 3 Color is satisfied, false otherwise.
         * @author David Slay
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
         * Finds if there is a solution to the 3 Color problem with this
         * UndirectedGraph. This is done by finding some solution where each
         * vertex in the graph is assigned one of three colors (red, blue, and
         * green), and no two vertices with the same color are allowed to be
         * adjacent to one another.
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
         * @param vertexIndex The current vertex index into the vertexColors
         * ArrayList.
         * @return True if the graph is 3 colorable, false otherwise.
         * @author David Slay
         */
        private boolean isConvertableTo3ColorHelper(int vertexIndex) {
            // base case: we have assigned a color to every vertex in the graph
            if (vertexIndex == this.numVertices) {
                // see if this assignment satisfies 3Color
                return is3ColorSatisfied();
            }

            // try setting this vertex to be green
            this.vertexColors.set(vertexIndex, Color.GREEN);
            if (isConvertableTo3ColorHelper(vertexIndex + 1)) { // recurse
                // we found a solution
                return true;
            }

            // try setting this vertex to be blue
            this.vertexColors.set(vertexIndex, Color.BLUE);
            if (isConvertableTo3ColorHelper(vertexIndex + 1)) { // recurse
                // we found a solution
                return true;
            }

            // try setting this vertex to be green
            this.vertexColors.set(vertexIndex, Color.RED);
            if (isConvertableTo3ColorHelper(vertexIndex + 1)) { // recurse
                // we found a solution
                return true;
            }

            // if none of these assignments work from this point, we backtrack
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
