package transaction.server.transaction;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import transaction.comm.Message;
import transaction.comm.MessageTypes;
import static transaction.comm.MessageTypes.TRANSACTION_ABORTED;
import transaction.server.TransactionServer;
import transaction.server.account.Account;
import transaction.server.lock.TransactionAbortedException;
import utils.TerminalColors;
import static utils.TerminalColors.ABORT_COLOR;
import static utils.TerminalColors.READ_COLOR;
import static utils.TerminalColors.RESET_COLOR;
import static utils.TerminalColors.WRITE_COLOR;

/**
 * Class representing the (singleton) transaction manager
 *
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class TransactionManager implements MessageTypes, TerminalColors {

    // counter for transaction IDs
    private static int transactionIdCounter = 0;

    // lists of transactions    
    private static final ArrayList<Transaction> runningTransactions = new ArrayList<>();
    private static final ArrayList<Transaction> committedTransactions = new ArrayList<>();
    private static final ArrayList<Transaction> abortedTransactions = new ArrayList<>();

    /**
     * Default constructor, nothing to do
     */
    public TransactionManager() {
    }

    /**
     * Helper method returning currently running transactions
     *
     * @return the list of currently running transactions
     */
    public ArrayList<Transaction> getRunningTransactions() {
        return runningTransactions;
    }

    /**
     * Helper method returning committed transactions
     *
     * @return the list of aborted transactions
     */
    public ArrayList<Transaction> getCommittedTransactions() {
        return committedTransactions;
    }

    /**
     * Helper method returning aborted transactions
     *
     * @return the list of aborted transactions
     */
    public ArrayList<Transaction> getAbortedTransactions() {
        return abortedTransactions;
    }

    /**
     * Run the transaction for an incoming client request
     *
     * @param client Socket object representing connection to client
     */
    public void runTransaction(Socket client) {
        (new TransactionManagerWorker(client)).start();
    }

    /**
     * Objects of this inner class run transactions, one thread runs one
     * transaction on behalf of a client
     */
    public class TransactionManagerWorker extends Thread {

        // networking communication related fields
        Socket client = null;
        ObjectInputStream readFromNet = null;
        ObjectOutputStream writeToNet = null;
        Message message = null;

        // transaction related fields
        Transaction transaction = null;
        int accountNumber = 0;
        int balance = 0;

        // flag for jumping out of while loop after this transaction closed
        boolean keepgoing = true;

        // the constructor just opens up the network channels
        private TransactionManagerWorker(Socket client) {
            this.client = client;
            // setting up object streams
            try {
                readFromNet = new ObjectInputStream(client.getInputStream());
                writeToNet = new ObjectOutputStream(client.getOutputStream());
            } catch (IOException e) {
                System.out.println("[TransactionManagerWorker.run] Failed to open object streams");
                System.exit(1);
            }
        }

        @Override
        public void run() {
            String log;
            
            // loop is left when transaction closes
            while (keepgoing) {
                // reading message
                try {
                    message = (Message) readFromNet.readObject();
                } catch (IOException | ClassNotFoundException e) { //TODO server is shutting down here... what exception is it getting??
                    System.out.println("[TransactionManagerWorker.run] Client shut down, shutting down as well ...");

                    // e.printStackTrace();
  
                    // // Prints what exception has been thrown 
                    System.out.println(e);

                    // bail out
                    //TransactionServer.shutDown();
                    return;
                }

                // processing message
                switch (message.getType()) {
                    // -------------------------------------------------------------------------------------------
                    case OPEN_TRANSACTION:
                    // -------------------------------------------------------------------------------------------

                        // ...

                        // create a transaction based on the info received from the message
                        transaction = new Transaction(transactionIdCounter);

                        transactionIdCounter++;

                        // add transaction to running transactions for logging purposes
                        runningTransactions.add(transaction);

                        // TODO send transaction ID back to client yes
                        try
                        {
                            // send read request response back to client
                            writeToNet.writeObject(new Message(OPEN_TRANSACTION, transaction.getTransactionID()));
                        }
                        catch(Exception e)
                        {
                            transaction.log("[TransactionManager - open transaction] Error sending open transaction message");
                        }

                        // log creation
                        transaction.log("Transaction created");
                        
                        break;

                    // -------------------------------------------------------------------------------------------
                    case CLOSE_TRANSACTION:
                    // -------------------------------------------------------------------------------------------

                        // ...

                        // use lock manager to release all locks
                        TransactionServer.lockManager.unLock(transaction);

                        // remove it from running transactions and move it to committed transactions
                        runningTransactions.remove(transaction);
                        committedTransactions.add(transaction);

                        transaction.log("Closed");

                        // send message to client that transaction committed
                        try
                        {
                            transaction.log("trying to send close message to client");
                            
                            // send read request response back to client
                            writeToNet.writeObject(new Message(TRANSACTION_COMMITTED));

                            transaction.log("sent closed message to client");
                        }
                        catch(Exception e)
                        {
                            transaction.log("[TransactionManager - close transaction] Error sending close transaction message");
                        }

                        break;

                    // -------------------------------------------------------------------------------------------
                    case READ_REQUEST:
                    // -------------------------------------------------------------------------------------------

                        // ...

                        // get content of message
                        Integer accountNumber = (Integer)message.getContent();
                                                
                        try {
                            // ==================================================================>
                            balance = TransactionServer.accountManager.read(accountNumber, transaction);
                            // <==================================================================
                            
                            // ...
                            transaction.log("Processing read request");

                            try
                            {
                               // send read request response back to client
                                writeToNet.writeObject(new Message(READ_REQUEST_RESPONSE, balance));
                            }
                            catch(Exception e)
                            {
                                transaction.log("[TransactionManager - read request] Error sending read request response message");
                            }
                                                    
                        } catch (TransactionAbortedException ex) {

                            // ...`
                            transaction.log("Aborted");
                            
                            keepgoing = false;

                            // write before image to accounts
                            transaction.beforeImage.forEach( (a, b) ->
                            
                               { TransactionServer.accountManager.getAccount(a)._write(b); }

                            );

                            // low-level write to the accounts what the balance was before

                            // release all acquired locks (lock manager)
                            TransactionServer.lockManager.unLock(transaction);

                            try
                            {
                               // send message to client stating it aborted
                                writeToNet.writeObject(new Message(TRANSACTION_ABORTED));

                                // close streams
                                readFromNet.close();
                                writeToNet.close();
                                client.close();
                            }
                            catch(Exception e)
                            {
                                transaction.log("[TransactionManager - read request] Error sending abortion warning");
                            }
                        }

                        break;

                    // -------------------------------------------------------------------------------------------
                    case WRITE_REQUEST:
                        // -------------------------------------------------------------------------------------------
                        Object[] content = (Object[]) message.getContent();
                        accountNumber = ((Integer) content[0]);
                        balance = ((Integer) content[1]);
                        

                        try {
                            // ====================================================================================
                            TransactionServer.accountManager.write(accountNumber, transaction, balance);
                            // <===================================================================================

                            // ...
                            transaction.log("Processing write request");

                            try
                            {
                                // send write request response back to client
                                writeToNet.writeObject(new Message(WRITE_REQUEST_RESPONSE));
                            }
                            catch(Exception e)
                            {
                                transaction.log("[TransactionManager - write request] Error sending write request response message");
                            }

                        } catch (TransactionAbortedException ex) {

                            // ...
                            transaction.log("Aborted");
                            
                            keepgoing = false;

                            // write before image to accounts
                            transaction.beforeImage.forEach( (a, b) ->
                            
                                { TransactionServer.accountManager.getAccount(a)._write(b); }

                            );

                            // low-level write to the accounts what the balance was before

                            // release all acquired locks (lock manager)
                            TransactionServer.lockManager.unLock(transaction);

                            try
                            {
                                // send message to client stating it aborted
                                writeToNet.writeObject(new Message(TRANSACTION_ABORTED, transaction.transactionID));

                                // close streams
                                readFromNet.close();
                                writeToNet.close();
                                client.close();
                            }
                            catch(Exception e)
                            {
                                transaction.log("[TransactionManager - write request] Error sending abortion warning");
                            }
                        }


                        break;

                    // -------------------------------------------------------------------------------------------
                    case SHUTDOWN:
                        // -------------------------------------------------------------------------------------------

                        // client sent shutdown message, tell the server
                        TransactionServer.shutDown();

                        transaction.log("Shutting down...");

                        // bail out
                        keepgoing = false;

                        continue;

                    // -------------------------------------------------------------------------------------------
                    default:
                        // -------------------------------------------------------------------------------------------

                        System.out.println("[TransactionManagerWorker.run] Warning: Message type not implemented");
                }

                System.out.println("End loop\n");
            }
        }
    }
}
