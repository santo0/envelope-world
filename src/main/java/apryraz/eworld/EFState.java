package apryraz.eworld;

/**
 * Class that represents the world
 **/
public class EFState {
    /**
     * State of the Envelope Finder.
     * What the agent knows about the world.
     **/

    int wDim;
    String[][] matrix;

    /**
     * EFState constructor.
     *
     * @param dim Dimension of the world. dim X dim
     */
    public EFState(int dim) {
        wDim = dim;
        matrix = new String[wDim][wDim];
        initializeState();
    }

    /**
     * Given a coordenate, get its corresponding state.
     *
     * @param i X-axis position (row).
     * @param j Y-axis position (column).
     * @return The state of the given position.
     */
    String getPositionState(int i, int j) {
        return matrix[i - 1][j - 1];
    }

    /**
     * Initialize all the positions with the "?" state.
     * The "?" state means that it is possible that
     * there is an envelope in the given position.
     * The "X" state means that there is no envelope
     * in the given position.
     */
    public void initializeState() {
        for (int i = 0; i < wDim; i++) {
            for (int j = 0; j < wDim; j++) {
                matrix[i][j] = "?";
            }
        }
    }

    /**
     * Set the state of the given position with the
     * given state value.
     * It is assumed that the given positions and
     * value are valid.
     *
     * @param i   X-axis position (row).
     * @param j   Y-axis position (column).
     * @param val The new state value.
     */
    public void set(int i, int j, String val) {
        matrix[i - 1][j - 1] = val;
    }


    /**
     * Checks if the given object is equals to
     * the current EFState.
     *
     * @param obj The object that is going to be checked.
     * @return True if the given object and the current EFState are equals. Otherwise, false.
     */
    public boolean equals(Object obj) {
        EFState efstate2 = (EFState) obj;
        boolean status = true;

        for (int i = 0; i < wDim; i++) {
            for (int j = 0; j < wDim; j++) {
                if (!matrix[i][j].equals(efstate2.matrix[i][j]))
                    status = false;
            }
        }
        return status;
    }

    /**
     * Prints the current state.
     */
    public void printState() {
        System.out.println("FINDER => Printing Envelope world matrix");
        for (int i = wDim - 1; i > -1; i--) {
            System.out.print("\t#\t");
            for (int j = 0; j < wDim; j++) {
                System.out.print(matrix[i][j] + " ");
            }
            System.out.println("\t#");
        }
    }

}
