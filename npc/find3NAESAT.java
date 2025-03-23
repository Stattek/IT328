import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Math;
import java.util.Random;

/**
 * find3NAESAT driver class
 * 
 * This class houses the main method for this program, as well as file handling,
 * CNFFormula parsing, and recursive backtracking/helper methods.
 */
public class find3NAESAT {
    /**
     * Main method
     * 
     * @param args
     * @author Andrew Ott
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println(
                    "Usage: java Find3NAESAT <input_file>\n    where input_file is the input file to read 3CNFs from.");
            System.exit(1);
        }

        // Random for generating random assignments
        Random random = new Random();
        // command line file name handling
        String fileName = args[0];
        File file = new File(fileName);

        System.out.println("** Find 3NAESAT in " + fileName + " (by backtracking):\n");

        try {
            // Generating an ArrayList of all CNFFormulas from the file.
            ArrayList<CNFFormula> formulas = parseFile(file);

            int formulaCounter = 0;

            // For each formula
            for (CNFFormula formula : formulas) {
                formulaCounter += 1;

                long start = System.currentTimeMillis();

                // Testing if the CNF is 3NAESAT and storing the result
                boolean isSatisfiable = solve3NAESAT(formula);

                long end = System.currentTimeMillis();
                // Duration of the operation
                long duration = end - start;

                // Temporary string to help with formatting
                String temp = "";

                // First line output formatting
                System.out.println("3CNF No." + formulaCounter + ":[n=" + formula.getNumLiterals() + " k="
                        + formula.getNumClauses() + "]");

                // Second line output formatting, dependent upon if the current CNFFormula is
                // 3NAESAT
                if (isSatisfiable) {
                    System.out.print("(" + duration + " ms) NAE certificate = [");
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
                    } else
                        boolVal = "F";

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
     * Helper method for the backtrack recursive method.
     * 
     * Takes a CNFFormula object and initializes it's assignment to a boolean array
     * of NumLiterals + 1.
     * 
     * The method then recursively calls the backtrack method using the CNFFormula
     * object, starting at index (literal) 1.
     * 
     * @param formula - Current CNFFormula
     * @return True is the CNFFormula is 3NAE Satisfactory, and false otherwise.
     */
    private static boolean solve3NAESAT(CNFFormula formula) {
        // +1 because literals are indexed starting at 1 not 0
        boolean[] assignment = new boolean[formula.getNumLiterals() + 1];
        formula.setAssignment(assignment);

        // Start backtracking from x1
        return backtrack(formula, 1);
    }

    /**
     * Recursive back tracking method.
     * 
     * Takes a given CNFFormula and works through it's literal assignments
     * attempting to find a 3NAESAT solution.
     * 
     * The method recursively assigns boolean values to the CNFFormula's literals in
     * numerical order.
     * If a true boolean for a given literal fails to provide a solution, the method
     * will attempt the same literal
     * with a false boolean. If that false boolean fails, the method will back track
     * to the previous literal and
     * attempt to find the solution from there.
     * 
     * This process repeats until all literals have been assigned a value, and that
     * assignment is shown to be satisfiable;
     * or until the method has exhausted all possible assignments and fails to
     * produce a satisfiable result
     * 
     * @param formula      - A given CNFFormula
     * @param literalIndex - The index of the currently focused literal
     * @return If the CNFFormula is satisfiable
     */
    private static boolean backtrack(CNFFormula formula, int literalIndex) {
        // Base case: all variables assigned
        if (literalIndex > formula.getNumLiterals()) {
            // Check if this assignment satisfies 3NAESAT
            return formula.isNAESatisfied();
        }

        // Try assigning true
        formula.setAssignment(literalIndex, true);
        if (backtrack(formula, literalIndex + 1)) {
            return true;
        }

        // Try assigning false
        formula.setAssignment(literalIndex, false);
        if (backtrack(formula, literalIndex + 1)) {
            return true;
        }

        // if neither assignment works we backtrack
        return false;
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
