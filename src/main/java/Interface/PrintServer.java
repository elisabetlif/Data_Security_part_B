package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface PrintServer extends Remote {
    String print(String token, String refreshToken, String filename, String printer) throws RemoteException, Exception;

    List<String> queue(String token, String refreshToken, String printer) throws RemoteException, Exception;

    String topQueue(String token, String refreshToken, String printer, int job) throws RemoteException, Exception;

    String start(String token, String refreshToken) throws RemoteException, Exception;

    String stop(String token, String refreshToken) throws RemoteException, Exception;

    String restart(String token, String refreshToken) throws RemoteException, Exception;

    String status(String token, String refreshToken, String printer) throws RemoteException, Exception;

    String readConfig(String token, String refreshToken, String parameter) throws RemoteException, Exception;

    String setConfig(String token, String refreshToken, String parameter, String value) throws RemoteException, Exception;
}
