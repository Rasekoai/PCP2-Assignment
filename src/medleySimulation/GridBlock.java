// This is a modified version of GridBlock class
// The code was modified to ensure only one thread owns the grid by semaphore 
// Code edited by : Rasekoai Mokose
// Date: 31/08/2024
// The code was mainly written by
// M. M. Kuttel 2024 mkuttel@gmail.com

package medleySimulation;
import java.util.concurrent.Semaphore;


public class GridBlock {
   private final Semaphore semaphore = new Semaphore(1); // one swimmer allowed in the grid at a time
   private int isOccupied;
   private final boolean isStart;
   private int[] coords;
   
   
   GridBlock(boolean startBlock) {
    isStart = startBlock;
    isOccupied = -1;
   }

   GridBlock(int x, int y, boolean startBlock) {
    this(startBlock);
    coords = new int[]{x, y};
   }

   public int getX() {
    return coords[0];
   }

   public int getY() {
    return coords[1];
   }

   // Get a block
   public boolean get(int threadID) throws InterruptedException {
    if (!semaphore.tryAcquire()) { // If semaphore is occupied, return false
        return false;
      }
    if (isOccupied == threadID) return true; // Thread already in this block
      isOccupied = threadID; // Set ID to thread that got the block
      return true;
      }

// Release a block
   public void release() {
    isOccupied = -1;
    semaphore.release(); // Release semaphore
   }

   // Is a block already occupied?
   public boolean occupied() {
    return !semaphore.tryAcquire(); // If semaphore is occupied, return true
   }

   // Is a start block
   public boolean isStart() {
    return isStart;
   }
}
