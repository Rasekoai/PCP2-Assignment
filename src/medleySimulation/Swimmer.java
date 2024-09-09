// This is a modified version of swimmer class
// Code modified by : Rasekoai Mokose
// Date: 31/08/2024
// The code was mainly written by
// M. M. Kuttel 2024 mkuttel@gmail.com

package medleySimulation;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;
import java.awt.Color;
import java.util.concurrent.BrokenBarrierException;
import java.util.Random;

public class Swimmer extends Thread {

    // Synchronization primitives
    //private static CyclicBarrier barrier; // Synchronizes swimmers' movements
    private static Semaphore[] strokeSemaphores = new Semaphore[4]; // Controls access to swim strokes
    private static CountDownLatch backstrokeLatch; // Ensures all backstroke swimmers are ready

    // Shared resources
    public static StadiumGrid stadium; // Represents the swimming stadium
    private FinishCounter finish; // Tracks race completion

    // Swimmer properties
    GridBlock currentBlock; // Current position of the swimmer
    private Random rand;
    private int movingSpeed;
    private PeopleLocation myLocation; // Tracks swimmer's location
    private int ID; // Unique identifier for the swimmer
    private int team; // Team identifier
    private GridBlock start; // Starting position

    // Enum representing different swim strokes
    public enum SwimStroke {
        Backstroke(1, 2.5, Color.black),
        Breaststroke(2, 2.1, new Color(255, 102, 0)),
        Butterfly(3, 2.55, Color.magenta),
        Freestyle(4, 2.8, Color.red);

        private final double strokeTime;
        private final int order;
        private final Color colour;

        SwimStroke(int order, double sT, Color c) {
            this.strokeTime = sT;
            this.order = order;
            this.colour = c;
        }

        public int getOrder() { return order; }
        public Color getColour() { return colour; }
    }

    private final SwimStroke swimStroke;

    // Initialize the backstroke latch for 10 teams
    static {
        backstrokeLatch = new CountDownLatch(10);
    }

    // Constructor
    Swimmer(int ID, int t, PeopleLocation loc, FinishCounter f, int speed, SwimStroke s) {
        // Initialize swimmer properties
        this.swimStroke = s;
        this.ID = ID;
        movingSpeed = speed;
        this.myLocation = loc;
        this.team = t;
        start = stadium.returnStartingBlock(team);
        finish = f;
        rand = new Random();
    }

    // Move the swimmer block by block
    private void moveBlockByBlock(int x, int y) throws InterruptedException, BrokenBarrierException {
        currentBlock = stadium.moveTowards(currentBlock, x, y, myLocation);
        // barrier.await(); // Synchronize with other swimmers (currently commented out)
    }

    // Getter methods
    public int getX() { return currentBlock.getX(); }
    public int getY() { return currentBlock.getY(); }
    public int getSpeed() { return movingSpeed; }
    public SwimStroke getSwimStroke() { return swimStroke; }

    // Swimmer enters the stadium
    public void enterStadium() throws InterruptedException {
        currentBlock = stadium.enterStadium(myLocation);
        sleep(100); // Pause briefly at the entrance
    }

    // Swimmer moves to the starting blocks
    public void goToStartingBlocks() throws InterruptedException {
        int x_st = start.getX();
        int y_st = start.getY();
       
        while (currentBlock != start) {
            sleep(movingSpeed * 3); // Move at a leisurely pace
            currentBlock = stadium.moveTowards(currentBlock, x_st, y_st, myLocation);
        }
        System.out.println("-----------Thread " + this.ID + " at start " + currentBlock.getX() + " " + currentBlock.getY());
        
        if (swimStroke == SwimStroke.Backstroke) {
            backstrokeLatch.countDown(); // Signal that this backstroke swimmer is ready
        }
    }

    // Swimmer dives into the pool
    private void dive() throws InterruptedException {
        int x = currentBlock.getX();
        int y = currentBlock.getY();
        currentBlock = stadium.jumpTo(currentBlock, x, y-2, myLocation);
    }

    // Swimmer performs the race
    private void swimRace() throws InterruptedException, BrokenBarrierException {
        int x = currentBlock.getX();
        // Swim to the end of the pool
        while ((boolean) ((currentBlock.getY()) != 0)) {
            moveBlockByBlock(x, 0);
            sleep((int) (movingSpeed * swimStroke.strokeTime));
        }
        // Swim back to the start
        while ((boolean) ((currentBlock.getY()) != (StadiumGrid.start_y - 1))) {
            moveBlockByBlock(x, StadiumGrid.start_y);
            sleep((int) (movingSpeed * swimStroke.strokeTime));
        }
    }

    // Swimmer exits the pool after finishing
    public void exitPool() throws InterruptedException {
        int bench = stadium.getMaxY() - swimStroke.getOrder(); // Calculate bench position
        int lane = currentBlock.getX() + 1; // Slightly offset from the lane
        currentBlock = stadium.moveTowards(currentBlock, lane, currentBlock.getY(), myLocation);
        while (currentBlock.getY() != bench) {
            currentBlock = stadium.moveTowards(currentBlock, lane, bench, myLocation);
            sleep(movingSpeed * 3); // Move at a leisurely pace
        }
    }

    // Main execution method for the swimmer thread
    public void run() {
        try {
            sleep(movingSpeed + (rand.nextInt(10))); // Random initial delay
            myLocation.setArrived();
           
            enterStadium();
            goToStartingBlocks();

            if (swimStroke == SwimStroke.Backstroke) {
                backstrokeLatch.await(); // Wait for all backstroke swimmers to be ready
            }

            dive();
            swimRace();
            
            if (swimStroke.order == 4) { // If this is the last swimmer (Freestyle)
                finish.finishRace(ID, team);
            } else {
                exitPool();
            }

        } catch (InterruptedException | BrokenBarrierException e1) {
            // Exception handling (currently empty)
        }
    }
}