// This is a modified version of swim Team class
// Code modified by : Rasekoai Mokose
// Date: 31/08/2024
// The code was mainly written by
// M. M. Kuttel 2024 mkuttel@gmail.com
// Class to represent a swim team - which has four swimmers
package medleySimulation;
import medleySimulation.Swimmer.SwimStroke;

public class SwimTeam extends Thread {
    
    // Shared stadium grid for all teams
    public static StadiumGrid stadium;
    
    // Array to hold the team's swimmers
    private Swimmer[] swimmers;
    
    // Team identifier
    private int teamNo;
    
    // Constant defining the number of swimmers in a team
    public static final int sizeOfTeam = 4;
    
    // Constructor for the SwimTeam
    SwimTeam(int ID, FinishCounter finish, PeopleLocation[] locArr) {
        this.teamNo = ID;
        swimmers = new Swimmer[sizeOfTeam];
        
        // Get all swim stroke types
        SwimStroke[] strokes = SwimStroke.values();
        
        // Get the starting block for this team
        stadium.returnStartingBlock(ID);
        
        // Initialize swimmers in the team
        for (int i = teamNo * sizeOfTeam, s = 0; i < ((teamNo + 1) * sizeOfTeam); i++, s++) {
            // Create a new PeopleLocation for each swimmer
            locArr[i] = new PeopleLocation(i, strokes[s].getColour());
            
            // Generate a random speed for the swimmer
            int speed = (int)(Math.random() * (3) + 30); // range of speeds 
            
            // Create a new Swimmer object
            swimmers[s] = new Swimmer(i, teamNo, locArr[i], finish, speed, strokes[s]);
        }
    }
    
    // The main execution method for the SwimTeam thread
    public void run() {
        try {
            // Start swimmer threads in order
            for (int s = 0; s < sizeOfTeam; s++) {
                swimmers[s].start();
                swimmers[s].join(); // Wait for the current swimmer to finish before starting the next
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}