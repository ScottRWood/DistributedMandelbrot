import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.*;

import java.io.*;
import java.util.*;

public class MandelbrotServerImpl extends UnicastRemoteObject implements MandelbrotServer {
    private boolean fail = false;
    private int nextRow = -1;

    private int iterX = 3000;
    private int iterY = 2000;
    private int curX = 0;

    private float minDimX = -2;
    private float maxDimX = 1;
    private float minDimY = -1;
    private float maxDimY = 1;

    private float stepX = (maxDimX - minDimX) / iterX;
    private float stepY = (maxDimY - minDimY) / iterY;

    int[][] iters = new int[iterX][iterY];

    MandelbrotServerImpl() throws RemoteException {
        for (int row = 0; row < iterX; row++) {
            for (int col = 0; col < iterY; col++) {
                iters[row][col] = -1;
            }
        }
    }

    /**
     * Gets row for processing
     * @return 2D array containing arrays of form [x, y, x_step]
     * @throws RemoteException
     */
    public synchronized float[][] getRow() throws RemoteException {
        if (curX != iterX) {
            float[][] arr = new float[iterY][3];
            for (int col = 0; col < iterY; col++) {
                float[] tmp = {minDimX + (curX * stepX), minDimY + (col * stepY), curX};
                arr[col] = tmp;
            }
            curX++;
            System.out.printf("Gave row %d to client", curX - 1);
            System.out.println();
            return arr;
        }
        if (fail) {
            nextRow = checkIters();
            if (nextRow != -1) {
                float[][] arr = new float[iterY][3];
                for (int col = 0; col < iterY; col++) {
                    float[] tmp = {minDimX + (nextRow * stepX), minDimY + (col * stepY), nextRow};
                    arr[col] = tmp;
                }
                return arr;
            }
        }

        return null;
    }

    /**
     * Enters a row into the iter array
     * @param row: row to be entered
     * @throws RemoteException
     */
    public synchronized void giveRow(float[][] row) throws RemoteException {
        for (int col = 0; col < iterY; col++) {
            int x = (int) row[col][3];
            iters[x][col] = (int)row[col][2];
        }
        System.out.printf("Received row %d from client", (int)row[0][3]);
        System.out.println();
        if (checkIters() == -1) {
            writeToFile();
        }
    }

    /**
     * Checks whether all values of iteration values have been filled
     * @return boolean flag
     */
    private synchronized int checkIters() {
        int full = -1;

        for (int row=0; row<iterX; row++) {
            for (int col=0; col<iterY; col++) {
                if (iters[row][col] == -1) {
                    full = row;
                    if (curX == iterX) {
                        fail = true;
                    }
                    break;
                }
            }
        }
        return full;
    }

    /**
     * Write results to results.txt
     */
    private void writeToFile() {
       PrintWriter writer;
       System.out.println("Writing results to file...");
       try {
           writer = new PrintWriter("results.txt");
           for (int row = 0; row < iterX; row++) {
               for (int col = 0; col < iterY; col++) {
                   String dataX = String.valueOf(minDimX + (row * stepX));
                   String dataY = String.valueOf(minDimY + (col * stepY));
                   String dataIters = String.valueOf(iters[row][col]);
                   writer.print(dataX + " " + dataY + " " + dataIters);
                   writer.println();
               }
           }
           writer.close();
           System.out.println("Results written");
       } catch (FileNotFoundException e){
           System.out.println("Error: " + e.getMessage());
       } catch (Exception e) {
               System.out.println("Couldn't close writer");
       }
    }

    /**
     * Print that a client has connected
     * @throws RemoteException
     */
    public void printConnection() throws RemoteException {
        System.out.println("New client connected");
    }

    public static void main(String args[]) {
        try {
            System.setSecurityManager(new SecurityManager());
            MandelbrotServerImpl mandelbrotServer = new MandelbrotServerImpl();
            Naming.rebind("MAND-SERVER", mandelbrotServer);
            System.out.println("Server Started...");
        } catch (java.net.MalformedURLException e) {
            System.out.println("Malformed URL: " + e.toString());
        } catch (RemoteException e) {
            System.out.println("Remote exception: " + e.toString());
        }
    }
}
