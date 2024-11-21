
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
    private static String username;

    public static void main(String[] args) throws RemoteException {

        setupServers();            
        logInUser("Alice", "Fall2019");

        System.out.println(username + ": " + printServer.start(accessToken));

        System.out.println(username + ": " + printServer.print(accessToken, "document.txt", "Printer1"));
        System.out.println(username + ": " + printServer.print(accessToken, "file.txt", "Printer1"));

        System.out.println(username + ": " + printServer.queue(accessToken, "Printer1"));
        
        System.out.println(username + ": " + authServer.logout(refreshToken, username));

        System.out.println(username + ": " + printServer.print(accessToken, "document2", "Printer1"));

        logInUser("George", "DefinitelyNotFred");

        System.out.println(username + ": " + printServer.print(accessToken, "file.txt", "Printer2"));

        System.out.println(username + ": " + printServer.stop(accessToken));

        System.out.println(username + ": " + authServer.logout(refreshToken, username));


        logInUser("GeorgeT", "ILoveManchesterUnited");

        System.out.println(username + ": " + printServer.setConfig(accessToken, "Paper", "A4"));

        addDelay();

        System.out.println(username + ": " + printServer.readConfig(accessToken, "Paper"));
        
        System.out.println(username + ": user refreshes for new access token");
        refreshAccessToken();

        System.out.println(username + ": " + printServer.readConfig(accessToken, "Paper"));

        addDelay();
        addDelay();

        System.out.println(username + ": " + printServer.setConfig(accessToken, "Paper", "A5"));
        System.out.println(username + ": " + authServer.logout(refreshToken, username));


        
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
            System.out.println("Username or password is incorrect");
        } else {
            System.out.println("User: "+ user + " is logged in");
            username = user;
            accessToken = authResponse.getAccessToken();
            refreshToken = authResponse.getRefreshToken();
        }
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



