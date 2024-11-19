package transaction.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import transaction.comm.Message;
import transaction.comm.MessageTypes;
import transaction.server.lock.TransactionAbortedException;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;


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

        // TODO maybe stream creation is failing here for some reason? This is where the client breaks
        // i thought the client was breaking because the serve shutdown but idk maybe not
        try 
        {
            // Clean up any previous connection
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
            if (writeToNet != null) {
                writeToNet.close();
            }
            if (readFromNet != null) {
                readFromNet.close();
            }

            // make connection to server
            dbConnection = new Socket(host, port);
            writeToNet = new ObjectOutputStream(dbConnection.getOutputStream());
            writeToNet.flush();
            readFromNet = new ObjectInputStream(dbConnection.getInputStream());

            // make message
            Message message = new Message(OPEN_TRANSACTION);

            // send message
            writeToNet.writeObject(message);

            // read response message
            message = (Message)readFromNet.readObject();

            transactionID = (Integer)message.getContent();
        }
        // catch (ConnectException e)
        // {
        //     System.out.print("\n[openTransaction] Transaction #" + transactionID + " ");
        //     System.out.println("Connection refused: Unable to reach server at " + host + ":" + port);
        //     System.out.println("Please ensure the server is running and accessible.");
        //     e.printStackTrace();
        // } 
        // catch (SocketTimeoutException e)
        // {
        //     System.out.print("\n[openTransaction] Transaction #" + transactionID + " ");
        //     System.out.println("Connection timed out: Server did not respond within the specified timeout.");
        //     e.printStackTrace();
        // } 
        // catch (IOException e)
        // {
        //     System.out.print("\n[openTransaction] Transaction #" + transactionID + " ");
        //     System.out.println("I/O error occurred when trying to connect to the server.");
        //     e.printStackTrace();
        // }
        catch(Exception e)
        {
            // cry about it I guess
            //System.out.println("\n[openTransaction] Transaction #" + transactionID + " failed to open streams or send message or receive message");

            System.out.println("[openTransaction] Failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(0); // TODO remove, temporarily here for testing purposes 
        }
        
        return transactionID;
    }

    
    /**
     * Requests this transaction to be closed.
     * 
     * @return the status, i.e. either TRANSACTION_COMMITTED or TRANSACTION_ABORTED
     */
    public int closeTransaction() {
        
        // ...
        int returnStatus = -1;

        try
        {
            // make message
            Message message = new Message(CLOSE_TRANSACTION, transactionID);

            // send message
            writeToNet.writeObject(message);

            // receive status response
            message = (Message)readFromNet.readObject();

            returnStatus = message.getType();

            // close streams
            writeToNet.close();
            readFromNet.close();
            dbConnection.close();

            Thread.sleep(1000);

            System.out.println("\nTransaction #" + transactionID + " closed connection");
        }
        catch(Exception e)
        {
            // cry about it I guess
            System.out.println("\n[closeTransaction] Transaction #" + transactionID + " failed to open streams or send message or receive message"); 
        }
        
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

        try
        {
            // send message to server and receive response
            writeToNet.writeObject(message);

            message = (Message)readFromNet.readObject();
        }
        catch(Exception e)
        {
            // cry about it I guess
            System.out.println("\n[read] Transaction #" + transactionID + " failed to open streams or send message or receive message"); 
        }        
        
        // return balance
        if(message.getType() == READ_REQUEST_RESPONSE)
        {
            return (Integer) message.getContent();        
        }
        else
        {
            try
            {
                // close streams
                if (writeToNet != null) writeToNet.close();
                if (readFromNet != null) readFromNet.close();
                if (dbConnection != null) dbConnection.close();

                Thread.sleep(1000);
            }
            catch (Exception e)
            {
                System.out.println("Failed to close streams in case of abortion");
            }

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
        try
        {
            // send message to server and receive response
            writeToNet.writeObject(message);
            message = (Message)readFromNet.readObject();
        }
        catch(Exception e)
        {
            // cry about it I guess
            System.out.println("\n[write] Transaction #" + transactionID + " failed to open streams or send message or receive message"); 
        }

        if(message.getType() == TRANSACTION_ABORTED)
        {
            // here we have an ABORT_TRANSACTION

            try
            {
                // close streams
                if (writeToNet != null) writeToNet.close();
                if (readFromNet != null) readFromNet.close();
                if (dbConnection != null) dbConnection.close();

                Thread.sleep(1000);
            }
            catch (Exception e)
            {
                System.out.println("Failed to close streams in case of abortion");
            }            

            throw new TransactionAbortedException();
        }
    }
}
