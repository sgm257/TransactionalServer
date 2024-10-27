
package transaction.comm;

/**
 * Interface [MessageTypes] Defines the different message types used in the application.
 * Any entity using objects of class Message needs to implement this interface.
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public interface MessageTypes {
    
    // Messages sent from client
    public static final int OPEN_TRANSACTION  = 1;
    public static final int CLOSE_TRANSACTION = 2;
    public static final int READ_REQUEST  = 3;
    public static final int WRITE_REQUEST = 4;
        
    // Messages sent from the server in response to a client request 
    public static final int OPEN_TRANSACTION_RESPONSE = 5;
    public static final int CLOSE_TRANSACTION_RESPONSE = 6;    
    public static final int READ_REQUEST_RESPONSE  = 7;
    public static final int WRITE_REQUEST_RESPONSE = 8; // no need
    
    // Flag sent from server in response to a client's CLOSE_TRANSACTION
    public static final int TRANSACTION_COMMITTED = 9;
    // Flag sent from server in response to a client's failed READ_REQUEST/WRITE_REQUEST
    public static final int TRANSACTION_ABORTED   = 10;

    // Message sent from the client to the server to signal shutdown
    public static final int SHUTDOWN   = 11;
}
