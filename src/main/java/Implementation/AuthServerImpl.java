package Implementation;

import Interface.AuthServer;
import lib.AuthenticationResponse;
import lib.SessionManager;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AuthServerImpl extends UnicastRemoteObject implements AuthServer {
    private SessionManager sManager;
    private static final Logger LOGGER = Logger.getLogger(AuthServerImpl.class.getName());

    static {
        try {
            FileHandler fileHandler = new FileHandler("auth_server.log", true); 
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false); // Disable logging to console
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructs a new AuthServerImpl instance.
     *
     * @param sManager The SessionManager instance for handling session tokens.
     * @throws RemoteException If an error occurs during the creation of the remote
     *                         object.
     */
    public AuthServerImpl(SessionManager sManager) throws RemoteException {
        super();
        this.sManager = sManager;
    }

    /**
     * Authenticates a user and generates access and refresh tokens upon successful
     * login.
     *
     * @param username The username
     * @param password The plaintext password
     * @return An AuthenticationResponse containing the access and refresh tokens if
     *         authentication succeeds,
     *         otherwise null.
     */
    @Override
    public AuthenticationResponse login(String username, String password) {
        PasswordProcessing processing = new PasswordProcessing();
        if (processing.passwordPros(username, password)) {
            String refreshToken = sManager.createRefreshToken(username);
            String accessToken = sManager.createAccessToken(username, refreshToken);

            LOGGER.info("User: " + username + " is logged in");
            return new AuthenticationResponse(accessToken, refreshToken);
        } else {
            LOGGER.warning("Server: Authentication failed. Username or password incorrect.");
            return null;
        }
    }

    /**
     * Logs out a user by invalidating their tokens.
     *
     * @param refreshToken The refresh token.
     * @param username     The username of the user to be logged out.
     * @return A message indicating the result of the logout operation.
     */
    @Override
    public String logout(String refreshToken, String username) {
        String name = sManager.validateRefreshToken(refreshToken);
        if (name == null){
            sManager.invalidateTokens(refreshToken);
        } else if (!name.equals(username)) {
            LOGGER.warning(
                    "Server: Unauthorized attempt detected - Refresh token validation failed. User not authorized.");
            return "User cant be logged out";
        } else {
            sManager.invalidateTokens(refreshToken);
        }

        LOGGER.info("User: " + username + " is logged out");
        return username + ": User logged out";
    }

    /**
     * Refreshes an expired access token using a valid refresh token.
     *
     * @param refreshToken The refresh token.
     * @param accessToken  The expired access token.
     * @return An AuthenticationResponse containing a new access token and refresh
     *         token if the operation succeeds,
     *         otherwise null.
     */
    @Override
    public AuthenticationResponse refreshAccessToken(String refreshToken, String accessToken) {
        String username = sManager.extractUsernameFromExpiredToken(accessToken);

        if (username == null) {
            LOGGER.warning("Server: Failed to extract username from expired access token.");
            logout(refreshToken, username);
            return null;
        }

        String validatedUsername = sManager.validateRefreshToken(refreshToken);
        if (validatedUsername == null || !validatedUsername.equals(username)) {
            LOGGER.warning("Server: Invalid refresh token.");
            logout(refreshToken, username);
            return null;
        }

        sManager.invalidateTokens(refreshToken);

        String newRefreshToken = sManager.createRefreshToken(username);
        String newAccessToken = sManager.createAccessToken(username, newRefreshToken);

        LOGGER.info("Server: New refresh token and access token created for " + username);
        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }
}
