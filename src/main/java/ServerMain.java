
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Implementation.AuthServerImpl;
import Implementation.PrintServerImpl;
import lib.SessionManager;

import java.rmi.RemoteException;


public class ServerMain {
    public static void main(String[] args) {
        try {
            Registry registry = LocateRegistry.createRegistry(1097);
            
            final long idleTimeout = 30 * 1000; // 30 seconds
            final long absoluteTimeout = 60 * 1000 + 30 * 1000; // 1.5 minutes
            SessionManager sManager = new SessionManager(idleTimeout, absoluteTimeout);

            
            AuthServerImpl authServer = new AuthServerImpl(sManager);
            PrintServerImpl printServer = new PrintServerImpl(sManager);

            registry.rebind("AuthServer", authServer);
            registry.rebind("PrintServer", printServer);
            System.out.println("PrintServer and AuthServer bound in registry and ready to accept requests.");
    
            synchronized (ServerMain.class) {
                try {
                    ServerMain.class.wait();
                } catch (InterruptedException e) {
                    System.err.println("Server interrupted: " + e.toString());
                }
            }
        } catch (RemoteException e) {
            System.err.println("Server exception: " + e.toString());
            e.printStackTrace();
        }
    }
}