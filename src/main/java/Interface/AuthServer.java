package Interface;

import java.rmi.Remote;
import java.rmi.RemoteException;

import lib.AuthenticationResponse;

public interface AuthServer extends Remote {
    AuthenticationResponse login(String username, String password) throws RemoteException;

    String logout(String refreshToken, String accessToken) throws RemoteException;

    AuthenticationResponse refreshAccessToken(String refreshToken, String accessToken) throws RemoteException;
}


