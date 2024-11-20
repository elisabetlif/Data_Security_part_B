package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface PrintServer extends Remote {
    String print(String token, String filename, String printer) throws RemoteException;

    String queue(String token, String printer) throws RemoteException;

    String topQueue(String token, String printer, int job) throws RemoteException;

    String start(String token) throws RemoteException;

    String stop(String token) throws RemoteException;

    String restart(String token) throws RemoteException;

    String status(String token, String printer) throws RemoteException;

    String readConfig(String token, String parameter) throws RemoteException;

    String setConfig(String token, String parameter, String value) throws RemoteException;
}
