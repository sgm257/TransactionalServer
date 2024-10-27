package transaction.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import transaction.comm.Message;
import transaction.comm.MessageTypes;
import transaction.server.lock.TransactionAbortedException;

/**
 * This class represents the proxy that acts on behalf of the transaction server on the client side.
 * It provides an implementation of the coordinator interface to the client, hiding the fact
 * that there is a network in between.
 * From the client's perspective, an object of this class IS the transaction.
 * @author wolfdieterotte
 */
public class TransactionServerProxy implements MessageTypes{

    String host = null;
    int port;

    private Socket dbConnection = null;
    private ObjectOutputStream writeToNet = null;
    private ObjectInputStream readFromNet = null;
    private Integer transactionID = 0;


    /**
     * Constructor
     * @param host IP address of the transaction server
     * @param port port number of the transaction server
     */
    TransactionServerProxy(String host, int port) {
        this.host = host;
        this.port = port;
    }


    /**
     * Opens a transaction
     * 
     * @return the transaction ID 
     */
    public int openTransaction() {

        // ...
        
        return transactionID;
    }

    
    /**
     * Requests this transaction to be closed.
     * 
     * @return the status, i.e. either TRANSACTION_COMMITTED or TRANSACTION_ABORTED
     */
    public int closeTransaction() {
        
        // ...
        
        return returnStatus;
    }

    
   /**
     * Reading a value from an account
     * 
     * @param accountNumber
     * @return the balance of the account
     * @throws transaction.server.lock.TransactionAbortedException
     */
    public int read(int accountNumber) throws TransactionAbortedException
    {
        Message message = new Message(READ_REQUEST, accountNumber);

        // ...
        
        if(message.getType() == READ_REQUEST_RESPONSE)
        {
            return (Integer) message.getContent();        }
        else
        {
            throw new TransactionAbortedException();
        }
    }

    
/**
 * Writing value to account
 * @param accountNumber
 * @param amount
 * 
 * @throws transaction.server.lock.TransactionAbortedException
 */
     public void write(int accountNumber, int amount) throws TransactionAbortedException
    {
        Object[] content = new Object[]{accountNumber, amount};
        Message message = new Message(WRITE_REQUEST, content);

        // ...

        if(message.getType() == TRANSACTION_ABORTED)
        {
            // here we have an ABORT_TRANSACTION
            throw new TransactionAbortedException();
        }
    }
}
