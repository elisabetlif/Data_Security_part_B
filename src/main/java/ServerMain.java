
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Implementation.AuthServerImpl;
import Implementation.PrintServerImpl;
import lib.SessionManager;

import java.rmi.RemoteException;


public class ServerMain {
    public static void main(String[] args) {
        try {
            // Start the RMI registry programmatically on port 1099
            Registry registry = LocateRegistry.createRegistry(1099);
            
            final long idleTimeout = 60 * 1000; // 1 minutes
            final long absoluteTimeout = 5 * 60 * 60 * 1000; // 5 hours
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


            // Custom RMIServerSocketFactory for configuring server sockets
            /*
             * RMIServerSocketFactory serverSocketFactory = new RMIServerSocketFactory() {
                @Override
                public ServerSocket createServerSocket(int port) throws IOException {
                    // You can add custom settings here
                    return new ServerSocket(port);
                }
            };
             */
            

            // Custom RMIClientSocketFactory for clients to connect to the server sockets
            //RMIClientSocketFactory clientSocketFactory = new CustomRMIClientSocketFactory();
