import java.rmi.*;

public interface MandelbrotServer extends Remote {
    float[][] getRow() throws RemoteException;
    void giveRow(float[][] row) throws RemoteException;

    void printConnection() throws RemoteException;
}
