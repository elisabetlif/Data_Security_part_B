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

    /**
     * Validates the access token 
     * @param token Access token
     * @return
     * @throws AuthenticationException
     */
    private Claims validateToken(String token) {
        System.out.println("Check token validation...");
        Claims claims = sManager.validateAccessToken(token);
        return claims;
    }

    public boolean hasPermission(Claims claims, String requiredPermission){
        List<String> permissions = claims.get("permissions", List.class);
        return permissions != null && permissions.contains(requiredPermission);
    }

    public boolean hasRole(Claims claims, List<String> requiredRole){
        String role = claims.get("role", String.class);
        return requiredRole.contains(role);
    }

   

    /**
     * prints file filename on the specified printer
     * @throws Exception 
     */
    @Override
    public String print(String token, String filename, String printer){
        if(!isRunning){
            System.err.println("Server: Printer not running, unable to commit action");
            return "Printer is not running";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "print";
        
        if(claims != null){
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
                printerQueues.putIfAbsent(printer, new LinkedList<>());
                printerQueues.get(printer).add(filename);
                System.out.println("Printing document: " + filename);
                return "Printer \"" + printer + " prints file \"" + filename;   
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to print.");
                return "Unauthorized: Insufficient role or permission to print.";
            }
        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }

    /**
     * Lists the print queue for a given printer on the user's display
     * in lines of the form !job number ? !filename?
     */
    @Override
    public String queue(String token, String printer){     
        if(!isRunning){
            return "Printer is not running";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "queue";

        if (claims != null) {
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
                List<String> queueList = new ArrayList<>();
                LinkedList<String> queue = printerQueues.get(printer);
                
                if (queue == null || queue.isEmpty()) {
                    return "The print queue for printer \"" + printer + "\" is empty.";
                }
    
                int jobNumber = 1;
                for (String filename : queue) {
                    queueList.add(jobNumber + " " + filename);
                    jobNumber++;
                }
                
                System.out.println("Pritner gives current queue");
                return "Printers current queue: " + queue.toString();
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to see queue.");
                return "Unauthorized: Insufficient role or permission to see queue.";
            }           

        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }

    /**
     * moves job to the top of the queue
     */
    @Override
    public String topQueue(String token, String printer, int job){
        if(!isRunning){
            return "Printer is not running";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "topQueue";

        if (claims != null) {
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
                LinkedList<String> queue = printerQueues.get(printer);

                if (queue == null || queue.isEmpty()) {
                    return "The print queue for printer \"" + printer + "\" is empty.";
                }
    
                String getJob = queue.get(job);
                queue.remove(job);
                queue.addFirst(getJob);
    
                System.out.println("Printer moves job to the top of the queue");
                return "Job \"" + job + "\" has been moved to of printer \"" + printer + "\" queue.";
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to top queue.");
                return "Unauthorized: Insufficient role or permission to change the queue.";
            }  
        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }

    /**
     * starts the print server
     * @throws RemoteException 
     */
    @Override
    public String start(String token) {
        if(isRunning){
            return "Printer is already running";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "start";

        if (claims != null) {
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
                isRunning = true;
                System.out.println("Printer has been started");
                return "Print has been started.";
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to see start.");
                return "Unauthorized: Insufficient role or permission to see start.";
            } 
        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }

    /**
     * stops the print server
     */
    @Override
    public String stop(String token){
        if(!isRunning){
            return "Printer is already stopped";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "stop";

        if (claims != null) {
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
                isRunning = false;
                System.out.println("Printer has been stopped");
                return "Print has been stopped.";    
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to stop.");
                return "Unauthorized: Insufficient role or permission to stop";
            }    
        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }

    /**
     * stops the print server, clears the print queue and starts the print server again
     * @throws RemoteException 
     */
    @Override
    public String restart(String token){
        if(isRunning){
            return "Print has not been started to be able to restart";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "restart";

        if (claims != null) {
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
                stop(token);

                this.printerQueues.clear();
                System.out.println("Printer clears queue");
    
                start(token);
                System.out.println("Printer has been restarted");
                return "Print has been restarted."; 
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to restart.");
                return "Unauthorized: Insufficient role or permission to restart.";
            } 
        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }

    /**
     * prints status of printer on the user's display
     */
    @Override
    public String status(String token, String printer){
        if(!isRunning){
            return "Printer is not running";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "status";

        if (claims != null) {
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
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
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to see status.");
                return "Unauthorized: Insufficient role or permission to see status.";
            } 
        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }

    /**
     * prints the value of the parameter on the print server to the user's display
     */
    @Override
    public String readConfig(String token, String parameter){
        if(!isRunning){
            return "Printer is not running";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "readConfig";

        if (claims != null) {
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
                String value = configParameters.get(parameter);
                if (value == null) {
                    value = "Configuration parameter \"" + parameter + "\" is not set.";
                } else {
                    value = "Configuration parameter \"" + parameter + "\": " + value;
                }
        
                System.out.println("Printer reads value of parameter");
                return value; 
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to readConfig.");
                return "Unauthorized: Insufficient role or permission to readConfig.";
            } 
        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }

    /**
     * sets the parameter on the print server to value
     */
    @Override
    public String setConfig(String token, String parameter, String value){
        if(!isRunning){
            return "Printer is not running";
        }
        Claims claims = validateToken(token);
        String requiredPermission = "setConfig";

        if (claims != null) {
            boolean permission = hasPermission(claims, requiredPermission);

            if(permission){
                configParameters.put(parameter, value);
                System.out.println("Printer sets value of parameter");
                return "Sets configuration parameter \"" + parameter + "\" to " + value; 
            } else {
                System.err.println("Unauthorized: Insufficient role or permission to setConfig.");
                return "Unauthorized: Insufficient role or permission to setConfig.";
            }
        } else {
            System.err.println("Server: Unauthorized print attempt detected - Access token validation failed. User not authorized.");
            return "User not authorized to do action";
        }
    }
}
