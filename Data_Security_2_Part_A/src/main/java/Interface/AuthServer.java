package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import lib.AuthenticationResponse;

public interface AuthServer extends Remote {
    AuthenticationResponse login(String username, String password) throws RemoteException;

    void logout(String refreshToken, String accessToken) throws RemoteException;

    AuthenticationResponse refreshAccessToken(String refreshToken) throws RemoteException;
}


