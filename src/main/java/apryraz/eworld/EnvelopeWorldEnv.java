

package apryraz.eworld;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.System.exit;


public class EnvelopeWorldEnv {
    /**
     * world dimension
     **/
    int WorldDim;

    /**
     * envelopes locations
     */
    Set<Position> envelopesLocations;

    /**
     * number of envelopes
     */
    int numEnvelopes;

    /**
     * Class constructor
     *
     * @param dim          dimension of the world
     * @param envelopeFile File with list of envelopes locations
     **/
    public EnvelopeWorldEnv(int dim, String envelopeFile) {

        WorldDim = dim;
        loadEnvelopeLocations(envelopeFile);
    }

    /**
     * Load the list of pirates locations
     *
     * @param envelopeFile name of the file that should contain a
     * set of envelope locations in a single line.
     **/
    public void loadEnvelopeLocations(String envelopeFile) {
        String[] positionList;
        String locations = "";
        try {
            BufferedReader br = new BufferedReader(new FileReader(envelopeFile));
            System.out.println("ENVELOPE LOCATIONS FILE OPENED ...");
            locations = br.readLine();
            br.close();
        } catch (FileNotFoundException ex) {
            System.out.println("MSG.   => Envelope locations file not found");
            exit(1);
        } catch (IOException ex) {
            Logger.getLogger(EnvelopeWorldEnv.class.getName()).log(Level.SEVERE,
                                                                null, ex);
            exit(2);
        }
        positionList = locations.split(" ");
        envelopesLocations = new LinkedHashSet<>();
        for (String location : positionList) {
            String[] coords = location.split(",");
            envelopesLocations.add(new Position(Integer.parseInt(coords[0]),
                                    Integer.parseInt(coords[1])));
        }
        numEnvelopes = envelopesLocations.size();
    }


    /**
     * Process a message received by the EFinder agent,
     * by returning an appropriate answer
     * It should answer to moveto and detectsat messages
     *
     * @param msg message sent by the Agent
     * @return a msg with the answer to return to the agent
     **/
    public AMessage acceptMessage(AMessage msg) {
        AMessage ans = new AMessage("voidmsg", "", "", "");

        msg.showMessage();
        if (msg.getComp(0).equals("moveto")) {
            int nx = Integer.parseInt(msg.getComp(1));
            int ny = Integer.parseInt(msg.getComp(2));

            if (withinLimits(nx, ny)) {
                ans = new AMessage("movedto",
                                    msg.getComp(1),
                                    msg.getComp(2), "");
            } else
                ans = new AMessage("notmovedto",
                                    msg.getComp(1),
                                    msg.getComp(2), "");
        } else if (msg.getComp(0).equals("detectsat")) {
            StringBuilder readings = new StringBuilder();
            for (int i = 0; i < 5; i++) {
                readings.append(0);
            }
            int nx = Integer.parseInt(msg.getComp(1));
            int ny = Integer.parseInt(msg.getComp(2));
            Position givenPosition = new Position(nx, ny);
            for (Position envLoc : envelopesLocations) {
                if (givenPosition.isOnTop(envLoc)) {
                    readings.replace(0, 1, "1");
                }
                if (givenPosition.isOnRight(envLoc)) {
                    readings.replace(1, 2, "1");
                }
                if (givenPosition.isOnBot(envLoc)) {
                    readings.replace(2, 3, "1");
                }
                if (givenPosition.isOnLeft(envLoc)) {
                    readings.replace(3, 4, "1");
                }
                if (givenPosition.isOnSite(envLoc)) {
                    readings.replace(4, 5, "1");
                }
            }
            ans = new AMessage("detectsat",
                    (msg.getComp(1)),
                    (msg.getComp(2)),
                    readings.toString());//Answer: "detectsat", x, y, DetectorValue
            System.out.printf("ANS: %s\n", readings.toString());
        } else {
            System.out.printf("ERROR: Unknown message type (%s)\n",
                                msg.getComp(0));
        }
        return ans;
    }


    /**
     * Check if position x,y is within the limits of the
     * WorldDim x WorldDim   world
     *
     * @param x x coordinate of agent position
     * @param y y coordinate of agent position
     * @return true if (x,y) is within the limits of the world
     **/
    public boolean withinLimits(int x, int y) {

        return (x >= 1 && x <= WorldDim && y >= 1 && y <= WorldDim);
    }

}
