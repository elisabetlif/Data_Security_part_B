package Implementation;

import Interface.AuthServer;
import lib.AuthenticationResponse;
import lib.SessionManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class AuthServerImpl extends UnicastRemoteObject implements AuthServer {
    private SessionManager sManager;

    public AuthServerImpl(SessionManager sManager) throws RemoteException {
        super();
        this.sManager = sManager;
    }

    @Override
    public AuthenticationResponse login(String username, String password) {
        PasswordProcessing processing = new PasswordProcessing();
        if (processing.passwordPros(username, password)) {
            String refreshToken = sManager.createRefreshToken(username);
            String accessToken = sManager.createAccessToken(username, refreshToken);
            
            System.out.println("User: " + username + " is logged in");
            return new AuthenticationResponse(accessToken, refreshToken);
        } else {
            System.err.println("Server: Authentication failed.");
            return null;
        }
    }

    @Override
    public String logout(String refreshToken, String username) {
        String name = sManager.validateRefreshToken(refreshToken) ;
        if(!name.equals(username)){
            System.err.println("Server: Unauthorized attempt detected - Refresh token validation failed. User not authorized.");
            return "User cant be logged out";
        }
        sManager.invalidateTokens(refreshToken);

        System.out.println("User: " + username + "is logged out");
        return "User logged out";
    }

    @Override
    public AuthenticationResponse refreshAccessToken(String refreshToken, String accessToken){
        String username = sManager.extractUsernameFromExpiredToken(accessToken);
        
        if (username == null) {
            System.err.println("Server: Failed to extract username from expired access token.");
            return null; 
        }
    
        String validatedUsername = sManager.validateRefreshToken(refreshToken);
        if (validatedUsername == null || !validatedUsername.equals(username)) {
            System.err.println("Server: Invalid refresh token.");
            return null;
        }
    
        sManager.invalidateTokens(refreshToken);
    
        String newRefreshToken = sManager.createRefreshToken(username);
        String newAccessToken = sManager.createAccessToken(username, newRefreshToken);
    
        System.out.println("New refresh token and access token created.");
        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }
}



