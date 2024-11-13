package Implementation;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

import Interface.PrintServer;
import io.jsonwebtoken.Claims;
import lib.SessionManager;


public class PrintServerImpl extends UnicastRemoteObject implements PrintServer {
    private Boolean isRunning;
    private SessionManager sManager;
    private Map<String, LinkedList<String>> printerQueues;
    private Map<String, String> configParameters;

    public PrintServerImpl(SessionManager sManager) throws RemoteException {
        super();
        isRunning = false;
        this.sManager = sManager;
        this.printerQueues = new HashMap<>();
        this.configParameters = new HashMap<>();
    }

    private Claims validateAccessToken(String token, String refreshToken) throws RemoteException {
        System.out.println("Token validation");
        Claims claims = sManager.validateAccessToken(token);
        if(sManager.validateRefreshToken(refreshToken) == null || claims == null) {
            throw new RemoteException("Invalid or expired token.");
        } else {
            System.out.println("Tokens valid");
            return claims;
        }
    }


    /**
     * prints file filename on the specified printer
     * @throws Exception 
     */
    @Override
    public String print(String token, String refreshToken, String filename, String printer) throws Exception {
        Claims claims = validateAccessToken(token, refreshToken);
        String username = claims.getSubject();

        if (!isRunning) {
            throw new RemoteException("Print server is not running.");
        }
        
        printerQueues.putIfAbsent(printer, new LinkedList<>());
        printerQueues.get(printer).add(filename);
        System.out.println("Printer prints filename");
        return "User \"" + username + "\" added file \"" + filename + "\" to printer \"" + printer + "\" queue.";
        
    }

    /**
     * Lists the print queue for a given printer on the user's display
     * in lines of the form !job number ? !filename?
     * @throws Exception 
     */
    @Override
    public List<String> queue(String token, String refreshToken, String printer) throws Exception {     
        Claims claims = validateAccessToken(token, refreshToken);

        List<String> queueList = new ArrayList<>();
        LinkedList<String> queue = printerQueues.get(printer);
        
        if (queue == null || queue.isEmpty()) {
            System.out.println("The print queue for printer \"" + printer + "\" is empty.");
            return queue;
        }

        int jobNumber = 1;
        for (String filename : queue) {
            queueList.add(jobNumber + " " + filename);
            jobNumber++;
        }
        
        System.out.println("Pritner prints current queue");
        return queue;
    }

    /**
     * moves job to the top of the queue
     * @throws Exception 
     */
    @Override
    public String topQueue(String token, String refreshToken, String printer, int job) throws RemoteException, Exception {
        Claims claims = validateAccessToken(token, refreshToken);
        String username = claims.getSubject();
        
        LinkedList<String> queue = printerQueues.get(printer);
        
        if (queue == null || queue.isEmpty()) {
            System.out.println("The print queue for printer \"" + printer + "\" is empty.");
        }

        String getJob = queue.get(job);
        queue.remove(job);
        queue.addFirst(getJob);

        System.out.println("Printer moves job to the top of the queue");
        return "User \"" + username + "\" moved job " + job + " to top of printer \"" + printer + "\" queue.";
    }

    /**
     * starts the print server
     * @throws RemoteException 
     */
    @Override
    public String start(String token, String refreshToken) throws RemoteException, Exception {
        Claims claims = validateAccessToken(token, refreshToken);
        String username = claims.getSubject();
        
        if(isRunning){
            throw new RemoteException("Print server is already running.");
        }

        isRunning = true;
        System.out.println("Printer started");
        return "User \"" + username + "\" started the printer.";
    }

    /**
     * stops the print server
     */
    @Override
    public String stop(String token, String refreshToken) throws RemoteException, Exception{
        Claims claims = validateAccessToken(token, refreshToken);
        String username = claims.getSubject();
        
        if(!isRunning){
            throw new RemoteException("Print server is already stop");
        }

        isRunning = false;
        System.out.println("Printer stopped");
        return "User \"" + username + "\" stopped the printer.";
    }

    /**
     * stops the print server, clears the print queue and starts the print server again
     * @throws RemoteException 
     */
    @Override
    public String restart(String token, String refreshToken) throws RemoteException, Exception {
        Claims claims = validateAccessToken(token, refreshToken);
        String username = claims.getSubject();

        if(!isRunning){
            throw new RemoteException("Print server is not started to be able to restart");
        }
        
        this.printerQueues.clear();
        isRunning = true;

        System.out.println("Printer restarted");
        return "User \"" + username + "\" restarted the printer.";
    }

    /**
     * prints status of printer on the user's display
     */
    @Override
    public String status(String token, String refreshToken, String printer) throws Exception {
        Claims claims = validateAccessToken(token, refreshToken);
        String username = claims.getSubject();
        
        LinkedList<String> queue = printerQueues.get(printer);
        String statusMessage;

        if (queue == null) {
            statusMessage = "Printer \"" + printer + "\" is not available.";
        } else if (queue.isEmpty()) {
            statusMessage = "Printer \"" + printer + "\" is available and has no jobs in the queue.";
        } else {
            statusMessage = "Printer \"" + printer + "\" is available with " + queue.size() + " job(s) in the queue.";
        }

        System.out.println("Printer prints status");
        return statusMessage;
    }

    /**
     * prints the value of the parameter on the print server to the user's display
     */
    @Override
    public String readConfig(String token, String refreshToken, String parameter) throws Exception{
        Claims claims = validateAccessToken(token, refreshToken);
        String username = claims.getSubject();
        
        String value = configParameters.get(parameter);
        if (value == null) {
            value = "Configuration parameter \"" + parameter + "\" is not set.";
        } else {
            value = "Configuration parameter \"" + parameter + "\": " + value;
        }

        System.out.println("Printer reads value of parameter");
        return value;
    }

    /**
     * sets the parameter on the print server to value
     */
    @Override
    public String setConfig(String token, String refreshToken, String parameter, String value) throws Exception {
        Claims claims = validateAccessToken(token, refreshToken);
        String username = claims.getSubject();

        configParameters.put(parameter, value);
        System.out.println("Printer sets value of parameter");
        return "User \"" + username + "\" set configuration parameter \"" + parameter + "\" to " + value;
    }
}
