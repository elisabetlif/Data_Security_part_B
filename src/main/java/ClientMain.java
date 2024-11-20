
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import Interface.AuthServer;
import Interface.PrintServer;
import lib.AuthenticationResponse;

public class ClientMain {
    private static String accessToken = "";
    private static String refreshToken = "";
    private static AuthServer authServer;
    private static PrintServer printServer;

    public static void main(String[] args) throws RemoteException {

        setupServers();            
        logInUser("Alice", "Fall2019");

        System.out.println(printServer.start(accessToken));

        System.out.println(printServer.print(accessToken, "document.txt", "Printer1"));
        System.out.println(printServer.print(accessToken, "file.txt", "Printer1"));
        System.out.println(printServer.print(accessToken, "assignment.txt", "Printer1"));

        System.out.println(printServer.queue(accessToken, "Printer1"));
        
        System.out.println(authServer.logout(refreshToken, "Alice"));

        System.out.println(printServer.print(accessToken, "document2", "Printer1"));

        logInUser("George", "DefinitelyNotFred");

        System.out.println(printServer.print(accessToken, "file.txt", "Printer2"));

        System.out.println(printServer.stop(accessToken));

        addDelay();

        System.out.println(printServer.print(accessToken, "file2.txt", "Printer2"));
        System.out.println("User refreshes to be able to do action");

        refreshAccessToken();

        System.out.println(printServer.print(accessToken, "file2.txt", "Printer2"));
        
        synchronized (ServerMain.class) {
            try {
                ServerMain.class.wait();
            } catch (InterruptedException e) {
                System.err.println("Server interrupted: " + e.toString());
            }
        }
    }

    private static void setupServers(){
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            authServer = (AuthServer) registry.lookup("AuthServer");
            printServer = (PrintServer) registry.lookup("PrintServer");
        } catch (Exception e) {
            System.err.println("Error setting up servers: " + e.getMessage());
        }
    }

    private static void logInUser(String user, String password) throws RemoteException {
        AuthenticationResponse authResponse = authServer.login(user, password);
        if(authResponse == null){
            System.out.println("User: " + user + " is able to log in");
        } else {
            System.out.println("User: "+ user + " is logged in");
        }
        accessToken = authResponse.getAccessToken();
        refreshToken = authResponse.getRefreshToken();
    }

    private static void refreshAccessToken() throws RemoteException {
        AuthenticationResponse authResponse = authServer.refreshAccessToken(refreshToken, accessToken);
    
        if (authResponse == null) {
            System.out.println("Refresh token expired or invalid. Please log in again.");
            logInUser("George", "DefinitelyNotFred");
        } else {
            System.out.println("Access token refreshed.");
            accessToken = authResponse.getAccessToken();
            refreshToken = authResponse.getRefreshToken();
        }
    }

    private static void addDelay() {
        try {
            Thread.sleep(60000); // 60 seconds
        } catch (InterruptedException e) {
            System.err.println("Sleep interrupted: " + e.getMessage());
        }
    }
}



