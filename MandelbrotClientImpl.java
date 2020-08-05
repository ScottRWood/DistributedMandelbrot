import java.rmi.*;
import java.util.Arrays;

public class MandelbrotClientImpl {

    /**
     * Calculates mandelbrot iterations of a value.
     * @param z0_Re: real component of value
     * @param z0_Im: imaginary component of value
     * @return number of iterations
     */
    private static int mand(float z0_Re, float z0_Im) {
        float z_Re = z0_Re;
        float z_Im = z0_Im;

        int niter = 0;
        while (niter < 100) {
            float z_sq_re = z_Re*z_Re - z_Im*z_Im;
            float z_sq_im = (float) 2.0*z_Re*z_Im;
            float mod_z_sq_sq = z_sq_re*z_sq_re + z_sq_im*z_sq_im;
            if (mod_z_sq_sq > 4) break;
            z_Re = z_sq_re + z0_Re;
            z_Im = z_sq_im + z0_Im;
            niter++;
        }
        return niter;
    }

    /**
     * Calculates mandelbrot for an array of values
     * @param row: 2D float array containing arrays of form [x, y, x_step]
     * @return array of form [x, y, iters, x_step]
     */
    private static float[][] mandRow(float[][] row) {
        float[][] arr = new float[row.length][4];
        System.out.printf("Calculating for row %d", (int)row[0][2]);
        System.out.println();
        for (int col = 0; col < row.length; col++) {
            float[] tmp = {row[col][0], row[col][1], mand(row[col][0], row[col][1]), row[col][2]};
            arr[col] = tmp;
        }
        return arr;
    }

    public static void main(String[] args) {
        System.setSecurityManager(new SecurityManager());

        try {
            String url = "//localhost/MAND-SERVER";
            MandelbrotServer remoteObject = (MandelbrotServer) Naming.lookup(url);
            remoteObject.printConnection();
            while (true) {
                float[][] row = remoteObject.getRow();
                if (row == null) {
                    break;
                }
                remoteObject.giveRow(mandRow(row));
            }
            System.out.println("Finished");
        } catch (RemoteException e) {
            System.out.println("Error in lookup: " + e.toString());
        } catch (java.net.MalformedURLException e) {
            System.out.println("Malformed URL: " + e.toString());
        } catch (java.rmi.NotBoundException e) {
            System.out.println("Not bound: " + e.toString());
        }
    }
}
