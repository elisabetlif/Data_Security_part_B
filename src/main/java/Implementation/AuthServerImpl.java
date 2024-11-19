package Implementation;

import Interface.AuthServer;
import lib.AuthenticationResponse;
import lib.SessionManager;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class AuthServerImpl extends UnicastRemoteObject implements AuthServer {
    private SessionManager sManager;

    public AuthServerImpl(SessionManager sManager) throws RemoteException {
        super();
        this.sManager = sManager;
    }


    public boolean hasPermission(String token,String requiredPermission){
        Claims claims = sManager.validateAccessToken(token);
        List<String> permissions = claims.get("permissions", List.class);
        return permissions != null && permissions.contains(requiredPermission);
    }

    public boolean hasRole(String token, String requiredRole){
        Claims claims = sManager.validateAccessToken(token);
        String role = claims.get("role", String.class);
        return requiredRole.equals(role);
    }

    public boolean isAuthorized(String token, String requiredRole, String requiredPermission){
        return hasRole(token, requiredRole) && hasPermission(token, requiredPermission);
    }

    public 

    
    @Override
    public AuthenticationResponse login(String username, String password) throws RemoteException {
        PasswordProcessing processing = new PasswordProcessing();
        if (processing.passwordPros(username, password)) {
            String accessToken = sManager.createAccessToken(username);
            String refreshToken = sManager.createRefreshToken(username);

            return new AuthenticationResponse(accessToken, refreshToken);
        } else {
            throw new RemoteException("Authentication failed.");
        }
    }

    @Override
    public void logout(String refreshToken, String accessToken) throws RemoteException {
        sManager.invalidateTokens(refreshToken, accessToken);
    }

    @Override
    public AuthenticationResponse refreshAccessToken(String refreshToken) throws RemoteException {
        String username = sManager.validateRefreshToken(refreshToken);
        if (username == null) {
            throw new RemoteException("Invalid or expired refresh token.");
        }

        String newAccessToken = sManager.createAccessToken(username);

        // Optionally, issue a new refresh token (token rotation)
        String newRefreshToken = sManager.createRefreshToken(username);
        sManager.invalidateRefreshToken(refreshToken); // Invalidate old refresh token

        return new AuthenticationResponse(newAccessToken, newRefreshToken);
    }
}

public class UnauthorizedAccessException extends RuntimeException{
    public UnauthorizedAccessException(){
        System.out.println("User not authorized to perform this action");
    }
}

