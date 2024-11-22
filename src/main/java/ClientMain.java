
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
        logInUser("Alice", "Fall2017");
        logInUser("Alice", "Fall2019");

        System.out.println(printServer.start(accessToken));
        System.out.println(printServer.print(accessToken, "document.txt", "Printer1"));
        System.out.println(printServer.print(accessToken, "file.txt", "Printer1"));
        System.out.println(printServer.queue(accessToken, "Printer1"));
        System.out.println(authServer.logout(refreshToken, username));
        System.out.println(printServer.print(accessToken, "document2", "Printer1"));

        logInUser("George", "DefinitelyNotFred");

        System.out.println(printServer.print(accessToken, "file.txt", "Printer2"));
        System.out.println(printServer.stop(accessToken));
        System.out.println(authServer.logout(refreshToken, username));

        logInUser("GeorgeT", "ILoveManchesterUnited");

        System.out.println(printServer.setConfig(accessToken, "Paper", "A4"));

        System.out.println("----------------------------------------------------");
        System.out.println("Delay for 1 minutes");
        System.out.println("----------------------------------------------------");
        addDelay();

        System.out.println(printServer.readConfig(accessToken, "Paper"));

        System.out.println("--------------------------------------------------");
        System.out.println(username + ": user refreshes for new access token");
        System.out.println("-----------------------------------------------------");
        refreshAccessToken();

        System.out.println(printServer.readConfig(accessToken, "Paper"));

        System.out.println("----------------------------------------------------");
        System.out.println("Delay for 2 minutes");
        System.out.println("----------------------------------------------------");
        addDelay();
        addDelay();

        System.out.println(printServer.setConfig(accessToken, "Paper", "A5"));
        
        System.out.println("--------------------------------------------------");
        System.out.println(username + ": user refreshes for new access token");
        System.out.println("-----------------------------------------------------");
        refreshAccessToken();

        synchronized (ServerMain.class) {
            try {
                ServerMain.class.wait();
            } catch (InterruptedException e) {
                System.err.println("Server interrupted: " + e.toString());
            }
        }
    }

    /**
     * Sets up connection to remote servers.
     */
    private static void setupServers() {
        try {
            Registry registry = LocateRegistry.getRegistry("localhost", 1097);

            authServer = (AuthServer) registry.lookup("AuthServer");
            printServer = (PrintServer) registry.lookup("PrintServer");
        } catch (Exception e) {
            System.err.println("Error setting up servers: " + e.getMessage());
        }
    }

    /**
     * Logs in a user by interacting with the AuthServer
     * 
     * @param user     The username
     * @param password The password
     * @throws RemoteException If an error occurs during remote method invocation.
     */
    private static void logInUser(String user, String password) throws RemoteException {
        AuthenticationResponse authResponse = authServer.login(user, password);
        if (authResponse == null) {
            System.out.println("Username or password is incorrect");
        } else {
            System.out.println("User: " + user + " is logged in");
            username = user;
            accessToken = authResponse.getAccessToken();
            refreshToken = authResponse.getRefreshToken();
        }
    }

    /**
     * Refreshes the access token using the refresh token
     * 
     * @throws RemoteException If an error occurs during remote method invocation.
     */
    private static void refreshAccessToken() throws RemoteException {
        AuthenticationResponse authResponse = authServer.refreshAccessToken(refreshToken, accessToken);

        if (authResponse == null) {
            System.out.println("Refresh token expired or invalid. Please log in again.");
        } else {
            System.out.println("Access token refreshed.");
            accessToken = authResponse.getAccessToken();
            refreshToken = authResponse.getRefreshToken();
        }
    }

    /**
     * Adds a delay of one minute.
     */
    private static void addDelay() {
        try {
            Thread.sleep(60000); // 60 seconds
        } catch (InterruptedException e) {
            System.err.println("Sleep interrupted: " + e.getMessage());
        }
    }
}
