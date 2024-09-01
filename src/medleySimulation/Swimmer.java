//M. M. Kuttel 2024 mkuttel@gmail.com
//Class to represent a swimmer swimming a race
//Swimmers have one of four possible swim strokes: backstroke, breaststroke, butterfly and freestyle
package medleySimulation;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.BrokenBarrierException;

import java.awt.Color;
import java.util.Random;



public class Swimmer extends Thread {

    private static CyclicBarrier barrier; // Add a CyclicBarrier
    private static final Semaphore entranceSemaphore = new Semaphore(1); // Add a Semaphore with 1 permit for entrance
    private static final Semaphore[] strokeSemaphores = new Semaphore[4]; // Add a Semaphore array for swim strokes

    public static StadiumGrid stadium; //shared 
    private final FinishCounter finish; //shared


    GridBlock currentBlock;
    private final Random rand;
    private final int movingSpeed;

    private final PeopleLocation myLocation;
    private int ID; //thread ID 
    private final int team; // team ID
    private final GridBlock start;

    public enum SwimStroke {
        Backstroke(1,2.5,Color.black),
        Breaststroke(2,2.1,new Color(255,102,0)),
        Butterfly(3,2.55,Color.magenta),
        Freestyle(4,2.8,Color.red);

        private final double strokeTime;
        private final int order; // in minutes
        private final Color colour;

        SwimStroke( int order, double sT, Color c) {
            this.strokeTime = sT;
            this.order = order;
            this.colour = c;
        }

        public int getOrder() {return order;}

        public  Color getColour() { return colour; }
    }
    private final SwimStroke swimStroke;
    // Static block to initialize the semaphores and latch
    static {
        for (int i = 0; i < strokeSemaphores.length; i++) {
            strokeSemaphores[i] = new Semaphore(1);
        }
        // Add a CountDownLatch for backstroke swimmers
        CountDownLatch backstrokeLatch = new CountDownLatch(10); // 10 teams
    }

    //Constructor
    Swimmer( int ID, int t, PeopleLocation loc, FinishCounter f, int speed, SwimStroke s) {
        this.swimStroke = s;
        this.ID=ID;
        movingSpeed=speed; //range of speeds for swimmers
        this.myLocation = loc;
        this.team=t;
        start = stadium.returnStartingBlock(team);
        finish=f;
        rand=new Random();
    }
    private void moveBlockByBlock(int x, int y) throws InterruptedException, BrokenBarrierException {
        currentBlock = stadium.moveTowards(currentBlock, x, y, myLocation);
        barrier.await(); // Wait for all swimmers to reach this block
    }


    //getter
    public   int getX() { return currentBlock.getX();}

    //getter
    public   int getY() {	return currentBlock.getY();	}

    //getter
    public   int getSpeed() { return movingSpeed; }


    public SwimStroke getSwimStroke() {
        return swimStroke;
    }

    //!!!You do not need to change the method below!!!
    //swimmer enters stadium area
    public void enterStadium() throws InterruptedException {
        entranceSemaphore.acquire(); // Acquire the semaphore before entering
        currentBlock = stadium.enterStadium(myLocation); //
        sleep(200); // wait a bit at door, look around
        entranceSemaphore.release(); // Release the semaphore after entering
    }

    //!!!You do not need to change the method below!!!
    //go to the starting blocks
    //printlns are left here for help in debugging
    public void goToStartingBlocks() throws InterruptedException {
        int x_st= start.getX();
        int y_st= start.getY();
        strokeSemaphores[swimStroke.getOrder() - 1].acquire(); // Acquire the semaphore for the swimmer's stroke

        //System.out.println("Thread "+this.ID + " has start position: " + x_st  + " " +y_st );
        // System.out.println("Thread "+this.ID + " at " + currentBlock.getX()  + " " +currentBlock.getY() );
        while (currentBlock!=start) {
            //	System.out.println("Thread "+this.ID + " has starting position: " + x_st  + " " +y_st );
            //	System.out.println("Thread "+this.ID + " at position: " + currentBlock.getX()  + " " +currentBlock.getY() );
            sleep(movingSpeed*3);  //not rushing 
            currentBlock=stadium.moveTowards(currentBlock,x_st,y_st,myLocation); //head toward starting block
            //	System.out.println("Thread "+this.ID + " moved toward start to position: " + currentBlock.getX()  + " " +currentBlock.getY() );
        }
        System.out.println("-----------Thread "+this.ID + " at start " + currentBlock.getX()  + " " +currentBlock.getY() );
    }

    //!!!You do not need to change the method below!!!
    //dive in to the pool
    private void dive() throws InterruptedException {
        int x= currentBlock.getX();
        int y= currentBlock.getY();
        currentBlock=stadium.jumpTo(currentBlock,x,y-2,myLocation);
    }

    //!!!You do not need to change the method below!!!
    //swim there and back
    private void swimRace() throws InterruptedException, BrokenBarrierException {
        int x = currentBlock.getX();
        while ((boolean) ((currentBlock.getY()) != 0)) {
            moveBlockByBlock(x, 0);
            sleep((int) (movingSpeed * swimStroke.strokeTime)); // swim
        }

        while ((boolean) ((currentBlock.getY()) != (StadiumGrid.start_y - 1))) {
            moveBlockByBlock(x, StadiumGrid.start_y);
            sleep((int) (movingSpeed * swimStroke.strokeTime)); // swim
        }
    }


    //!!!You do not need to change the method below!!!
    //after finished the race
    public void exitPool() throws InterruptedException {
        int bench=stadium.getMaxY()-swimStroke.getOrder(); 			 //they line up
        int lane = currentBlock.getX()+1;//slightly offset
        currentBlock=stadium.moveTowards(currentBlock,lane,currentBlock.getY(),myLocation);
        while (currentBlock.getY()!=bench) {
            currentBlock=stadium.moveTowards(currentBlock,lane,bench,myLocation);
            sleep(movingSpeed* 3L);  //not rushing 
        }
    }

    public void run() {
        try {

            //Swimmer arrives
            sleep(movingSpeed+(rand.nextInt(10))); //arriving takes a while
            myLocation.setArrived();
            enterStadium();

            goToStartingBlocks();

            dive();

            swimRace();
            if(swimStroke.order==4) {
                finish.finishRace(ID, team); // fnishline
            }
            else {
                //System.out.println("Thread "+this.ID + " done " + currentBlock.getX()  + " " +currentBlock.getY() );			
                exitPool();//if not last swimmer leave pool
            }

        } catch (InterruptedException e1) {  //do nothing
        } catch (BrokenBarrierException e) {
            throw new RuntimeException(e);
        }
    }

}
