

package apryraz.eworld;

import java.util.ArrayList;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import static java.lang.System.exit;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.sat4j.core.VecInt;

import org.sat4j.specs.*;
import org.sat4j.minisat.*;


/**
 * This agent performs a sequence of movements, and after each
 * movement it "senses" from the evironment the resulting position
 * and then the outcome from the smell sensor, to try to locate
 * the position of Envelope
 **/
public class EnvelopeFinder {


    /**
     * The list of steps to perform
     **/
    ArrayList<Position> listOfSteps;
    /**
     * index to the next movement to perform, and total number of movements
     **/
    int idNextStep, numMovements;
    /**
     * Array of clauses that represent conclusiones obtained in the last
     * call to the inference function, but rewritten using the "past" variables
     **/
    ArrayList<VecInt> futureToPast = null;
    /**
     * the current state of knowledge of the agent (what he knows about
     * every position of the world)
     **/
    EFState efstate;
    /**
     * The object that represents the interface to the Envelope World
     **/
    EnvelopeWorldEnv EnvAgent;
    /**
     * SAT solver object that stores the logical boolean formula with the rules
     * and current knowledge about not possible locations for Envelope
     **/
    ISolver solver;
    /**
     * Agent position in the world
     **/
    int agentX, agentY;
    /**
     * Dimension of the world and total size of the world (Dim^2)
     **/
    int WorldDim, WorldLinealDim;


    /**
     * Offset of past of positions with possible envelope.
     */
    int EnvelopePastOffset;
    /**
     * Offset of future of positions with possible envelope.
     */
    int EnvelopeFutureOffset;
    /**
     * Offset of reading 1 of positions.
     */
    int ReadOneOffset;
    /**
     * Offset of reading 2 of positions.
     */
    int ReadTwoOffset;
    /**
     * Offset of reading 3 of positions.
     */
    int ReadThreeOffset;
    /**
     * Offset of reading 4 of positions.
     */
    int ReadFourOffset;
    /**
     * Offset of reading 5 of positions.
     */
    int ReadFiveOffset;

    /**
     * Pointer of next literal to add.
     */
    int actualLiteral;


    /**
     * The class constructor must create the initial Boolean formula with the
     * rules of the Envelope World, initialize the variables for indicating
     * that we do not have yet any movements to perform, make the initial state.
     *
     * @param WDim        the dimension of the Envelope World.
     * @param environment the environment agent.
     **/
    public EnvelopeFinder(int WDim, EnvelopeWorldEnv environment) throws IOException {

        WorldDim = WDim;
        WorldLinealDim = WorldDim * WorldDim;
        EnvAgent = environment; //Set the environment agent.
        try {
            solver = buildGamma();
        } catch (IOException | ContradictionException ex) {
            Logger.getLogger(EnvelopeFinder.class.getName()).log(Level.SEVERE, null, ex);
        }
        numMovements = 0;
        idNextStep = 0;
        System.out.println("STARTING Envelope FINDER AGENT...");


        efstate = new EFState(WorldDim);  // Initialize state (matrix) of knowledge with '?'
        efstate.printState();
    }

    /**
     * Store a reference to the Environment Object that will be used by the
     * agent to interact with the Envelope World, by sending messages and getting
     * answers to them. This function must be called before trying to perform any
     * steps with the agent.
     *
     * @param environment the Environment object
     **/
    public void setEnvironment(EnvelopeWorldEnv environment) {
        EnvAgent = environment;
    }


    /**
     * Load a sequence of steps to be performed by the agent. This sequence will
     * be stored in the listOfSteps ArrayList of the agent.  Steps are represented
     * as objects of the class Position.
     *
     * @param numSteps  number of steps to read from the file
     * @param stepsFile the name of the text file with the line that contains
     *                  the sequence of steps: x1,y1 x2,y2 ...  xn,yn
     **/
    public void loadListOfSteps(int numSteps, String stepsFile) {
        String[] stepsList;
        String steps = ""; // Prepare a list of movements to try with the FINDER Agent
        try {
            BufferedReader br = new BufferedReader(new FileReader(stepsFile));
            System.out.println("STEPS FILE OPENED ...");
            steps = br.readLine();
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("MSG.   => Steps file not found");
            exit(1);
        } catch (IOException ex) {
            Logger.getLogger(EnvelopeFinder.class.getName()).log(Level.SEVERE, null, ex);
            exit(2);
        }
        stepsList = steps.split(" ");
        listOfSteps = new ArrayList<>(numSteps);
        for (int i = 0; i < numSteps; i++) {
            String[] coords = stepsList[i].split(",");
            listOfSteps.add(new Position(Integer.parseInt(coords[0]), Integer.parseInt(coords[1])));
        }
        numMovements = listOfSteps.size(); // Initialization of numMovements
        idNextStep = 0;
    }

    /**
     * Returns the current state of the agent.
     *
     * @return the current state of the agent, as an object of class EFState
     **/
    public EFState getState() {
        return efstate;
    }

    /**
     * Execute the next step in the sequence of steps of the agent, and then
     * use the agent sensor to get information from the environment. In the
     * original Envelope World, this would be to use the Smelll Sensor to get
     * a binary answer, and then to update the current state according to the
     * result of the logical inferences performed by the agent with its formula.
     **/
    public void runNextStep() throws
            IOException, ContradictionException, TimeoutException {

        // Add the conclusions obtained in the previous step
        // but as clauses that use the "past" variables
        addLastFutureClausesToPastClauses();

        // Ask to move, and check whether it was successful
        // Also, record if a pirate was found at that position
        processMoveAnswer(moveToNext());


        // Next, use Detector sensor to discover new information
        processDetectorSensorAnswer(DetectsAt());


        // Perform logical consequence questions for all the positions
        // of the Envelope World
        performInferenceQuestions();
        efstate.printState();      // Print the resulting knowledge matrix
    }


    /**
     * Ask the agent to move to the next position, by sending an appropriate
     * message to the environment object. The answer returned by the environment
     * will be returned to the caller of the function.
     *
     * @return the answer message from the environment, that will tell whether the
     * movement was successful or not.
     **/
    public AMessage moveToNext() {
        Position nextPosition;

        if (idNextStep < numMovements) {
            nextPosition = listOfSteps.get(idNextStep);
            idNextStep = idNextStep + 1;
            return moveTo(nextPosition.x, nextPosition.y);
        } else {
            System.out.println("NO MORE steps to perform at agent!");
            return (new AMessage("NOMESSAGE", "", "", ""));
        }
    }

    /**
     * Use agent "actuators" to move to (x,y)
     * We simulate this by telling to the World Agent (environment)
     * that we want to move, but we need the answer from it
     * to be sure that the movement was made with success
     *
     * @param x horizontal coordinate (row) of the movement to perform
     * @param y vertical coordinate (column) of the movement to perform
     * @return returns the answer obtained from the environment object to the
     * moveto message sent
     **/
    public AMessage moveTo(int x, int y) {
        // Tell the EnvironmentAgentID that we want  to move
        AMessage msg, ans;

        msg = new AMessage("moveto", (new Integer(x)).toString(), (new Integer(y)).toString(), "");
        ans = EnvAgent.acceptMessage(msg);
        System.out.println("FINDER => moving to : (" + x + "," + y + ")");

        return ans;
    }

    /**
     * Process the answer obtained from the environment when we asked
     * to perform a movement
     *
     * @param moveans the answer given by the environment to the last move message
     **/
    public void processMoveAnswer(AMessage moveans) {
        if (moveans.getComp(0).equals("movedto")) {
            agentX = Integer.parseInt(moveans.getComp(1));
            agentY = Integer.parseInt(moveans.getComp(2));

            System.out.println("FINDER => moved to : (" + agentX + "," + agentY + ")");
        }
    }

    /**
     * Send to the environment object the question:
     * "Does the detector sense something around(agentX,agentY) ?"
     *
     * @return return the answer given by the environment
     **/
    public AMessage DetectsAt() {
        AMessage msg, ans;

        msg = new AMessage("detectsat", (new Integer(agentX)).toString(),
                (new Integer(agentY)).toString(), "");
        ans = EnvAgent.acceptMessage(msg);
        System.out.println("FINDER => detecting at : (" + agentX + "," + agentY + ")");
        return ans;
    }


    /**
     * Process the answer obtained for the query "Detects at (x,y)?"
     * by adding the appropriate evidence clause to the formula
     *
     * @param ans message obtained to the query "Detects at (x,y)?".
     *            It will a message with three fields: DetectorValue x y
     *            <p>
     *            DetectorValue must be a number that encodes all the valid readings
     *            of the sensor given the envelopes in the 3x3 square around (x,y)
     **/
    public void processDetectorSensorAnswer(AMessage ans) throws
            IOException, ContradictionException, TimeoutException {
        if (ans.getComp(0).equals("detectsat")) {
            int x = Integer.parseInt(ans.getComp(1));
            int y = Integer.parseInt(ans.getComp(2));
            String detectorValue = ans.getComp(3);      //xxxxx s.t. rd1,rd2,rd3,rd4,rd5
            for (int i = 0; i < detectorValue.length(); i++) {
                char reading = detectorValue.charAt(i);

                if (reading == '0') {   //No reading received
                    VecInt evidence = new VecInt();
                    switch (i) {
                        case 0: //No reading 1
                            evidence.insertFirst(-coordToLineal(x, y, ReadOneOffset));
                            break;
                        case 1: //No reading 2
                            evidence.insertFirst(-coordToLineal(x, y, ReadTwoOffset));
                            break;
                        case 2: //No reading 3
                            evidence.insertFirst(-coordToLineal(x, y, ReadThreeOffset));
                            break;
                        case 3: //No reading 4
                            evidence.insertFirst(-coordToLineal(x, y, ReadFourOffset));
                            break;
                        case 4: //No reading 5
                            evidence.insertFirst(-coordToLineal(x, y, ReadFiveOffset));
                            break;
                    }
                    solver.addClause(evidence);
                } else if (reading == '1') {//Reading received
                    /*
                        In this case, the Envelope Finder is not interested
                        when the sensor receives a reading.
                     */
                } else {
                    System.out.printf("ERROR: Unknown code (%c)\n", reading);
                    exit(1);
                }
            }
        } else {
            System.out.printf("ERROR: Unknown message type (%s)\n", ans.getComp(0));
            exit(1);
        }
    }


    /**
     * This function should add all the clauses stored in the list
     * futureToPast to the formula stored in solver.
     * Use the function addClause( VecInt ) to add each clause to the solver
     **/
    public void addLastFutureClausesToPastClauses() throws IOException,
            ContradictionException, TimeoutException {
        if (futureToPast != null) {
            for (VecInt clause : futureToPast) {
                solver.addClause(clause); //Added conclusions to the solver.
            }
        }
        futureToPast = new ArrayList<>(); //Reset future conclusions.

    }

    /**
     * This function should check, using the future variables related
     * to possible positions of Envelope, whether it is a logical consequence
     * that an envelope is NOT at certain positions. This should be checked for all the
     * positions of the Envelope World.
     * The logical consequences obtained, should be then stored in the futureToPast list
     * but using the variables corresponding to the "past" variables of the same positions
     * <p>
     * An efficient version of this function should try to not add to the futureToPast
     * conclusions that were already added in previous steps, although this will not produce
     * any bad functioning in the reasoning process with the formula.
     **/
    public void performInferenceQuestions() throws IOException,
            ContradictionException, TimeoutException {
        for (int i = 1; i <= WorldDim; i++) {
            for (int j = 1; j <= WorldDim; j++) {

                int linealIndex = coordToLineal(i, j, EnvelopeFutureOffset);
                int linealIndexPast = coordToLineal(i, j, EnvelopePastOffset);

                VecInt variablePositive = new VecInt();
                variablePositive.insertFirst(linealIndex);

                if (!(solver.isSatisfiable(variablePositive))) {
                    VecInt concPast = new VecInt();
                    concPast.insertFirst(-(linealIndexPast));
                    futureToPast.add(concPast);
                    efstate.set(i, j, "X");
                }
            }
        }
    }

    /**
     * This function builds the initial logical formula of the agent and stores it
     * into the solver object.
     *
     * @return returns the solver object where the formula has been stored
     **/
    public ISolver buildGamma() throws UnsupportedEncodingException,
            FileNotFoundException, IOException, ContradictionException {
        int totalNumVariables;

        /**
         * nxn past positions + nxn future positions + nxn reading 1 positions + nxn reading 2 positions
         * + nxn reading 3 positions + nxn reading 4 positions + nxn reading 5 positions
         * */
        totalNumVariables = WorldLinealDim * 7; //n*n*7
        solver = SolverFactory.newDefault();
        solver.setTimeout(3600);
        solver.newVar(totalNumVariables);
        // This variable is used to generate, in a particular sequential order,
        // the variable indentifiers of all the variables
        actualLiteral = 1;

        //at least one envelope at past; one clause
        pastAloEnvelope();
        //at least one envelope at future; one clause
        futureAloEnvelope();
        // keep past to future consistency; nxn clauses
        pastToFuture();
        // no reading 1; approx nxnx3 clauses (off limits position not added)
        noReadingOneEnvelope();
        // no reading 2; approx nxnx3 clauses (off limits position not added)
        noReadingTwoEnvelope();
        //no reading 3; approx nxnx3 clauses (off limits position not added)
        noReadingThreeEnvelope();
        //no reading 4; approx nxnx3 clauses (off limits position not added)
        noReadingFourEnvelope();
        //no reading 5; nxn clauses
        noReadingFiveEnvelope();

        return solver;
    }

    /**
     * Adds to solver the following clauses:
     * For all the positions in the Envelope World,
     * if in a given position the agent hasn't received
     * the reading 1, then the agent will know for sure
     * that in the positions influenced by reading 1,
     * there won't be envelopes.
     *
     * @throws ContradictionException
     */
    void noReadingOneEnvelope() throws ContradictionException {
        ReadOneOffset = actualLiteral;
        for (int i = 1; i <= WorldDim; i++) {
            for (int j = 1; j <= WorldDim; j++) {
                int[][] reading = {{i + 1, j - 1}, {i + 1, j}, {i + 1, j + 1}};
                int detectLiteral = coordToLineal(i, j, ReadOneOffset);
                for (int k = 0; k < reading.length; k++) {
                    if (EnvAgent.withinLimits(reading[k][0], reading[k][1])) {
                        VecInt readClause = new VecInt();
                        int futureEnvelopeLiteral = coordToLineal(reading[k][0], reading[k][1], EnvelopeFutureOffset);
                        readClause.insertFirst(-futureEnvelopeLiteral);
                        readClause.insertFirst(detectLiteral);
                        solver.addClause(readClause);
                    }
                }
                actualLiteral++;
            }
        }
    }

    /**
     * Adds to solver the following clauses:
     * For all the positions in the Envelope World,
     * if in a given position the agent hasn't received
     * the reading 2, then the agent will know for sure
     * that in the positions influenced by reading 2,
     * there won't be envelopes.
     *
     * @throws ContradictionException
     */
    void noReadingTwoEnvelope() throws ContradictionException {
        ReadTwoOffset = actualLiteral;
        for (int i = 1; i <= WorldDim; i++) {
            for (int j = 1; j <= WorldDim; j++) {
                int[][] reading = {{i - 1, j + 1}, {i, j + 1}, {i + 1, j + 1}};
                int detectLiteral = coordToLineal(i, j, ReadTwoOffset);
                for (int k = 0; k < reading.length; k++) {
                    if (EnvAgent.withinLimits(reading[k][0], reading[k][1])) {
                        VecInt readClause = new VecInt();
                        int futureEnvelopeLiteral = coordToLineal(reading[k][0], reading[k][1], EnvelopeFutureOffset);
                        readClause.insertFirst(-futureEnvelopeLiteral);
                        readClause.insertFirst(detectLiteral);
                        solver.addClause(readClause);
                    }
                }
                actualLiteral++;
            }
        }
    }

    /**
     * Adds to solver the following clauses:
     * For all the positions in the Envelope World,
     * if in a given position the agent hasn't received
     * the reading 3, then the agent will know for sure
     * that in the positions influenced by reading 3,
     * there won't be envelopes.
     *
     * @throws ContradictionException
     */
    void noReadingThreeEnvelope() throws ContradictionException {
        ReadThreeOffset = actualLiteral;
        for (int i = 1; i <= WorldDim; i++) {
            for (int j = 1; j <= WorldDim; j++) {
                int[][] reading = {{i - 1, j - 1}, {i - 1, j}, {i - 1, j + 1}};
                int detectLiteral = coordToLineal(i, j, ReadThreeOffset);
                for (int k = 0; k < reading.length; k++) {
                    if (EnvAgent.withinLimits(reading[k][0], reading[k][1])) {
                        VecInt readClause = new VecInt();
                        int futureEnvelopeLiteral = coordToLineal(reading[k][0], reading[k][1], EnvelopeFutureOffset);
                        readClause.insertFirst(-futureEnvelopeLiteral);
                        readClause.insertFirst(detectLiteral);
                        solver.addClause(readClause);
                    }
                }
                actualLiteral++;
            }
        }
    }

    /**
     * Adds to solver the following clauses:
     * For all the positions in the Envelope World,
     * if in a given position the agent hasn't received
     * the reading 4, then the agent will know for sure
     * that in the positions influenced by reading 4,
     * there won't be envelopes.
     *
     * @throws ContradictionException
     */
    void noReadingFourEnvelope() throws ContradictionException {
        ReadFourOffset = actualLiteral;
        for (int i = 1; i <= WorldDim; i++) {
            for (int j = 1; j <= WorldDim; j++) {
                int[][] reading = {{i - 1, j - 1}, {i, j - 1}, {i + 1, j - 1}};
                int detectLiteral = coordToLineal(i, j, ReadFourOffset);
                for (int k = 0; k < reading.length; k++) {
                    if (EnvAgent.withinLimits(reading[k][0], reading[k][1])) {
                        VecInt readClause = new VecInt();
                        int futureEnvelopeLiteral = coordToLineal(reading[k][0], reading[k][1], EnvelopeFutureOffset);
                        readClause.insertFirst(-futureEnvelopeLiteral);
                        readClause.insertFirst(detectLiteral);
                        solver.addClause(readClause);
                    }
                }
                actualLiteral++;
            }
        }
    }

    /**
     * Adds to solver the following clauses:
     * For all the positions in the Envelope World,
     * if in a given position the agent hasn't received
     * the reading 5, then the agent will know for sure
     * that in the current position there won't be envelope.
     *
     * @throws ContradictionException
     */
    void noReadingFiveEnvelope() throws ContradictionException {
        ReadFiveOffset = actualLiteral;
        for (int i = 1; i <= WorldDim; i++) {
            for (int j = 1; j <= WorldDim; j++) {
                VecInt readClause = new VecInt();
                int detectLiteral = coordToLineal(i, j, ReadFiveOffset);
                int futureEnvelopeLiteral = coordToLineal(i, j, EnvelopeFutureOffset);
                readClause.insertFirst(-futureEnvelopeLiteral);
                readClause.insertFirst(detectLiteral);
                solver.addClause(readClause);
                actualLiteral++;
            }
        }
    }


    /**
     * Adds to solver the clauses to keep consistency.
     *
     * @throws ContradictionException
     */
    void pastToFuture() throws ContradictionException {
        VecInt impClause;
        for (int i = 0; i < WorldLinealDim; i++) {//nxn
            impClause = new VecInt();
            impClause.insertFirst(i + EnvelopePastOffset);
            impClause.insertFirst(-(i + EnvelopeFutureOffset));
            solver.addClause(impClause);
        }
    }

    /**
     * Adds to solver the ALO envelope in past clause.
     *
     * @throws ContradictionException
     */
    void pastAloEnvelope() throws ContradictionException {
        EnvelopePastOffset = actualLiteral;
        VecInt aloClause = new VecInt();
        for (int i = 0; i < WorldLinealDim; i++) {
            aloClause.insertFirst(actualLiteral);
            actualLiteral++;
        }
        solver.addClause(aloClause);
    }

    /**
     * Adds to solver the ALO envelope in future clause.
     *
     * @throws ContradictionException
     */
    void futureAloEnvelope() throws ContradictionException {
        EnvelopeFutureOffset = actualLiteral;
        VecInt aloClause = new VecInt();
        for (int i = 0; i < WorldLinealDim; i++) {
            aloClause.insertFirst(actualLiteral);
            actualLiteral++;
        }
        solver.addClause(aloClause);
    }


    /**
     * Convert a coordinate pair (x,y) to the integer value  t_[x,y]
     * of variable that stores that information in the formula, using
     * offset as the initial index for that subset of position variables
     * (past and future position variables have different variables, so different
     * offset values)
     *
     * @param x      x coordinate of the position variable to encode
     * @param y      y coordinate of the position variable to encode
     * @param offset initial value for the subset of position variables
     *               (past or future subset)
     * @return the integer indentifer of the variable  b_[x,y] in the formula
     **/
    public int coordToLineal(int x, int y, int offset) {
        return ((x - 1) * WorldDim) + (y - 1) + offset;
    }

    /**
     * Perform the inverse computation to the previous function.
     * That is, from the identifier t_[x,y] to the coordinates  (x,y)
     * that it represents
     *
     * @param lineal identifier of the variable
     * @param offset offset associated with the subset of variables that
     *               lineal belongs to
     * @return array with x and y coordinates
     **/
    public int[] linealToCoord(int lineal, int offset) {
        lineal = lineal - offset + 1;
        int[] coords = new int[2];
        coords[1] = ((lineal - 1) % WorldDim) + 1;
        coords[0] = (lineal - 1) / WorldDim + 1;
        return coords;
    }


}
