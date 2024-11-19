import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.rmi.RemoteException;

import Interface.AuthServer;
import Interface.PrintServer;
import lib.AuthenticationResponse;

public class ClientMain {
    private static String accessToken = "";
    private static String refreshToken = "";
    private static AuthServer authServer;
    private static PrintServer printServer;

    public static void main(String[] args) {
        try {
            // Connect to the RMI registry running on localhost at port 1099
            Registry registry = LocateRegistry.getRegistry("localhost", 1099);

            // Lookup the remote objects
            authServer = (AuthServer) registry.lookup("AuthServer");
            printServer = (PrintServer) registry.lookup("PrintServer");

            // Login and obtain the tokens
            AuthenticationResponse authResponse = authServer.login("George", "DefinitelyNotFred");
            accessToken = authResponse.getAccessToken();
            refreshToken = authResponse.getRefreshToken();

            // Start the print server
            String start = executeMethod(() -> printServer.start(accessToken, refreshToken));
            System.out.println(start);

            // Perform print operations
            String print1 = executeMethod(() -> printServer.print(accessToken, refreshToken, "document.txt", "Printer1"));
            String print2 = executeMethod(() -> printServer.print(accessToken, refreshToken, "file.txt", "Printer1"));
            String print3 = executeMethod(() -> printServer.print(accessToken, refreshToken, "assignment.txt", "Printer1"));
            

            System.out.println(print1);
            System.out.println(print2);
            System.out.println(print3);

            // View the print queue
            List<String> queue = executeMethod(() -> printServer.queue(accessToken, refreshToken, "Printer1"));
            System.out.println(queue.toString());

            List<String> topQueue = executeMethod(() -> printServer.queue(accessToken, refreshToken, "Printer1"));
            System.out.println(topQueue.toString());

            // Logout from the authentication server
            authServer.logout(refreshToken, accessToken);

            // Attempt to print after logout (should fail)
            String printLogout = executeMethod(() -> printServer.print(accessToken, refreshToken, "document2", "Printer1"));
            System.out.println(printLogout);

        } catch (Exception e) {
            System.err.println("Client exception: " + e.toString());
            e.printStackTrace();
        }
    }

    private static <T> T executeMethod(RemoteMethod<T> method) throws Exception {
        try {
            return method.execute();
        } catch (RemoteException e) {
            if (e.getMessage().contains("Invalid or expired access token")) {
                refreshAccessToken();
                return method.execute();
            } else {
                throw e;
            }
        }
    }

    private static void refreshAccessToken() throws Exception {
        AuthenticationResponse authResponse = authServer.refreshAccessToken(refreshToken);
        System.out.println(accessToken == authResponse.getAccessToken());
        accessToken = authResponse.getAccessToken();
        refreshToken = authResponse.getRefreshToken(); 
    }

    // Functional interface for methods returning a value
    @FunctionalInterface
    interface RemoteMethod<T> {
        T execute() throws Exception;
    }

    // Functional interface for void methods
    @FunctionalInterface
    interface RemoteVoidMethod {
        void execute() throws Exception;
    }
}

/**
 * 
 * 
 *     // Helper method to handle methods that return a value
    private static <T> T executeMethod(RemoteMethod<T> method) throws Exception {
        try {
            return method.execute();
        } catch (RemoteException e) {
            if (e.getMessage().contains("Invalid or expired access token")) {
                refreshAccessToken();
                return method.execute();
            } else {
                throw e;
            }
        }
    }
 */



