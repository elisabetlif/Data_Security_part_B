package Implementation;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import Interface.PrintServer;
import io.jsonwebtoken.Claims;
import lib.SessionManager;


public class PrintServerImpl extends UnicastRemoteObject implements PrintServer {
    private static final Logger LOGGER = Logger.getLogger(PrintServerImpl.class.getName());
    private Boolean isRunning;
    private SessionManager sManager;

    static {
        try {
            FileHandler fileHandler = new FileHandler("print_server.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
            LOGGER.setUseParentHandlers(false); // Disable logging to console
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
   
    /**
    * Constructor for PrintServerImpl.
    * 
    * @param sManager the SessionManager instance for handling authentication tokens
    * @throws RemoteException if a remote communication error occurs
    */
    public PrintServerImpl(SessionManager sManager) throws RemoteException {
        super();
        isRunning = false;
        this.sManager = sManager;
    }

    /**
    * Validates the access token.
    * 
    * @param token the access token to be validated
    * @return the Claims object extracted from the token
    */
    private Claims validateToken(String token) {
        Claims claims = sManager.validateAccessToken(token);
        return claims;
    }

    public boolean hasPermission(Claims claims, String requiredPermission){
        List<String> permissions = claims.get("permissions", List.class);
        return permissions != null && permissions.contains(requiredPermission);
    }

    /**
    * Prints the specified file on the specified printer.
    * 
    * @param token    the access token for authentication
    * @param filename the name of the file to print
    * @param printer  the name of the printer to use
    * @return a message indicating the result of the operation
    */
    @Override
    public String print(String token, String filename, String printer){
        if(!isRunning){
            LOGGER.warning("Server: Printer not running, unable to commit action");
            return "Printer is not running";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "print";
        
        if(claim != null){
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){
                LOGGER.info(claim.getSubject() + ": print");
                return claim.getSubject() + ": print";     
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to print");
                return "Unauthorized: Insufficient role or permission to print";
            }
        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }

    /**
    * Lists the print queue for a given printer.
    * 
    * @param token   the access token for authentication
    * @param printer the name of the printer
    * @return a message indicating the result of the operation
    */
    @Override
    public String queue(String token, String printer){     
        if(!isRunning){
            LOGGER.warning("Server: Printer not running, unable to commit action");
            return "Printer is not running";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "queue";

        if (claim != null) {
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){                
                LOGGER.info(claim.getSubject() + ": queue");
                return claim.getSubject() + ": queue"; 
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to queue");
                return "Unauthorized: Insufficient role or permission to queue";
            }           

        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }

    /**
    * Moves a print job to the top of the queue for the specified printer.
    * 
    * @param token   the access token for authentication
    * @param printer the name of the printer
    * @param job     the job number to move to the top
    * @return a message indicating the result of the operation
    */
    @Override
    public String topQueue(String token, String printer, int job){
        if(!isRunning){
            LOGGER.warning("Server: Printer not running, unable to commit action");
            return "Printer is not running";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "topQueue";

        if (claim != null) {
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){    
                LOGGER.info(claim.getSubject() + ": topqueue");
                return claim.getSubject() + ": topqueue";
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to topqueue");
                return "Unauthorized: Insufficient role or permission to topqueue";
            }  
        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }

    /**
    * Starts the printer.
    * 
    * @param token   the access token for authentication
    * @return a message indicating the result of the operation
    */
    @Override
    public String start(String token) {
        if(isRunning){
            LOGGER.warning("Server: Printer is alreadt running, unable to commit action");
            return "Printer is already running";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "start";

        if (claim != null) {
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){
                isRunning = true;
                LOGGER.info(claim.getSubject() + ": start");
                return claim.getSubject() + ": start";  
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to start");
                return "Unauthorized: Insufficient role or permission to start";
            } 
        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }

    /**
    * Stops the printer.
    * 
    * @param token   the access token for authentication
    * @return a message indicating the result of the operation
    */
    @Override
    public String stop(String token){
        if(!isRunning){
            LOGGER.warning("Server: Printer is already not running, unable to commit action");
            return "Printer is already stopped";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "stop";

        if (claim != null) {
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){
                isRunning = false;
                LOGGER.info(claim.getSubject() + ": stop");
                return claim.getSubject() + ": stop";  
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to stop.");
                return "Unauthorized: Insufficient role or permission to stop";
            }    
        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }

    /**
    * Restarts the printer.
    * 
    * @param token   the access token for authentication
    * @return a message indicating the result of the operation
    */
    @Override
    public String restart(String token){
        if(isRunning){
            LOGGER.warning("Server: Printer not running, unable to commit action");
            return "Print has not been started to be able to restart";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "restart";

        if (claim != null) {
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){
                LOGGER.info(claim.getSubject() + ": restart");
                return claim.getSubject() + ": restart";    
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to restart.");
                return "Unauthorized: Insufficient role or permission to restart.";
            } 
        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }

    /**
    * Prints the status on the specific printer.
    * 
    * @param token   the access token for authentication
    * @param printer the name of the printer
    * @return a message indicating the result of the operation
    */
    @Override
    public String status(String token, String printer){
        if(!isRunning){
            LOGGER.warning("Server: Printer not running, unable to commit action");
            return "Printer is not running";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "status";

        if (claim != null) {
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){
                LOGGER.info(claim.getSubject() + ": status");
                return claim.getSubject() + ": status";    
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to status");
                return "Unauthorized: Insufficient role or permission to status";
            } 
        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }

    /**
    * Retrieves the value of a configuration parameter from the print server.
    * 
    * @param token     the access token for authentication
    * @param parameter the name of the configuration parameter to read
    * @return a message indicating the result of the operation or the parameter value
    */
    @Override
    public String readConfig(String token, String parameter){
        if(!isRunning){
            LOGGER.warning("Server: Printer not running, unable to commit action");
            return "Printer is not running";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "readConfig";

        if (claim != null) {
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){
                LOGGER.info(claim.getSubject() + ": readConfig");
                return claim.getSubject() + ": readConfig"; 
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to readConfig");
                return "Unauthorized: Insufficient role or permission to readConfig";
            } 
        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }

    /**
    * Updates the value of a configuration parameter on the print server.
    * 
    * @param token     the access token for authentication
    * @param parameter the name of the configuration parameter to set
    * @param value     the new value to assign to the configuration parameter
    * @return a message indicating the result of the operation
    */
    @Override
    public String setConfig(String token, String parameter, String value){
        if(!isRunning){
            LOGGER.warning("Server: Printer not running, unable to commit action");
            return "Printer is not running";
        }
        Claims claim = validateToken(token);
        String requiredPermission = "setConfig";

        if (claim != null) {
            boolean permission = hasPermission(claim, requiredPermission);

            if(permission){
                LOGGER.info(claim.getSubject() + ": setConfig");
                return claim.getSubject() + ": setConfig";    
            } else {
                LOGGER.warning("Server: Unauthorized, insufficient role or permission to setConfig");
                return "Unauthorized: Insufficient role or permission to setConfig";
            }
        } else {
            LOGGER.warning("Server: Unauthorized - Access token validation failed. User not authorized.");
            return "User not authorized";
        }
    }
}
