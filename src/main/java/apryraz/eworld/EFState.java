package apryraz.eworld;

public class EFState {
    /**
     *
     **/

    int wDim;
    String[][] matrix;

    public EFState(int dim) {
        wDim = dim;
        matrix = new String[wDim][wDim];
        initializeState();
    }

    String getPositionState(int i, int j){
        return matrix[i - 1][j - 1];
    }

    public void initializeState() {
        for (int i = 0; i < wDim; i++) {
            for (int j = 0; j < wDim; j++) {
                matrix[i][j] = "?";
            }
        }
    }

    /* i is the row, j the column
       we assume i and j are given in the range [1,wDim] */
    public void set(int i, int j, String val) {

        matrix[i - 1][j - 1] = "\u001B[32m"+val+"\u001B[0m";
    }

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

    public void printState() {
        System.out.println("FINDER => Printing Envelope world matrix");
        for (int i = wDim - 1; i > -1; i--) {
            System.out.print("\t#\t");
            for (int j = 0; j < wDim; j++) {
                System.out.print(matrix[i][j] + " ");
//                System.out.print(matrix[i][j]+ "("+i+","+j+")" + " ");

            }
            System.out.println("\t#");
        }
    }

}
