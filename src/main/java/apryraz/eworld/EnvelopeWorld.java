package apryraz.eworld;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;

import org.sat4j.specs.*;
import org.sat4j.minisat.*;
import org.sat4j.reader.*;


/**
 * The class for the main program of the Envelope World
 **/
public class EnvelopeWorld {


    /**
     * This function should execute the sequence of steps stored in the file fileSteps,
     * but only up to numSteps steps. Each step must be executed with function
     * runNextStep() of the EnvelopeFinder agent.
     *
     * @param wDim          the dimension of world
     * @param numSteps      num of steps to perform
     * @param fileSteps     file name with sequence of steps to perform
     * @param fileEnvelopes file name with sequence of steps to perform
     * @throws IOException IoExeption error
     * @throws ContradictionException contradiction error
     * @throws TimeoutException time out exeption
     **/
    public static void runStepsSequence(int wDim,
                                        int numSteps, String fileSteps, String fileEnvelopes) throws
            IOException, ContradictionException, TimeoutException {
        // Make instances of EnvelopeFinder agent and environment object classes
        EnvelopeFinder EAgent;
        EnvelopeWorldEnv EnvAgent;

        EnvAgent = new EnvelopeWorldEnv(wDim, fileEnvelopes);
        EAgent = new EnvelopeFinder(wDim, EnvAgent);

        // load list of steps into the Finder Agent
        EAgent.loadListOfSteps(numSteps, fileSteps);


        // Execute sequence of steps with the Agent
        for (int i = 0; i < numSteps; i++) {
            EAgent.runNextStep();
        }
    }

    /**
     * This function should load five arguments from the command line:
     * @param args ...
     * arg[0] = dimension of the word
     * arg[1] = num of steps to perform
     * arg[2] = file name with sequence of steps to perform
     * arg[3] = file name with list of envelopes positions
     * @throws IOException IoExeption error
     * @throws ContradictionException contradiction error
     * @throws TimeoutException time out exeption
     * @throws ParseFormatException parseformat exeption
     * @throws IllegalArgumentException illegal argument exeption
     **/
    public static void main(String[] args) throws ParseFormatException,
            IOException, ContradictionException, TimeoutException, IllegalArgumentException {

        if (args.length != 4) {
            System.out.println("Usage: EnvelopeWorld wdim numSteps fileSteps fileEnvelopes");
            throw new IllegalArgumentException(String.format("Expected 4 arguments. Given %d", args.length));
        }
        int wDim = getWDim(args[0]);
        int numSteps = getNumSteps(args[1]);
        String fileSteps;
        if (new File(args[2]).exists()) {
            fileSteps = args[2];
        } else {
            System.out.printf("Path %s doesn't exist%n", args[2]);
            throw new IllegalArgumentException();
        }
        String fileEnvelopes;
        if (new File(args[2]).exists()) {
            fileEnvelopes = args[3];
        } else {
            System.out.printf("Path %s doesn't exist%n", args[3]);
            throw new IllegalArgumentException();
        }

        // Here I run a concrete example, but you should read parameters from
        // the command line, as decribed above.
        runStepsSequence(wDim, numSteps, fileSteps, fileEnvelopes);
    }

    /**
     *
     * @param arg arguments
     * @return wdimension
     * @throws IllegalArgumentException Illegal arguments
     */

    private static int getWDim(String arg) throws IllegalArgumentException {
        int wDim;
        try {
            wDim = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
        return wDim;
    }

    /**
     *
     * @param arg args
     * @return numsteps
     * @throws IllegalArgumentException illegal args
     */

    private static int getNumSteps(String arg) throws IllegalArgumentException {
        int numSteps;
        try {
            numSteps = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException();
        }
        return numSteps;
    }
}
