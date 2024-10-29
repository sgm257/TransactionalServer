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
            // loop is left when transaction closes
            while (keepgoing) {
                // reading message
                try {
                    message = (Message) readFromNet.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("[TransactionManagerWorker.run] Client shut down, shutting down as well ...");

                    // bail out
                    TransactionServer.shutDown();
                    return;
                }

                // processing message
                switch (message.getType()) {
                    // -------------------------------------------------------------------------------------------
                    case OPEN_TRANSACTION:
                    // -------------------------------------------------------------------------------------------

                        // ...

                        // create a transaction based on the info received from the message
                        Transaction t = new Transaction((int)message.getContent());

                        // add transaction to running transactions for logging purposes
                        runningTransactions.add(t);

                        // log creation
                        transaction.log("Transaction created");
                        
                        break;

                    // -------------------------------------------------------------------------------------------
                    case CLOSE_TRANSACTION:
                    // -------------------------------------------------------------------------------------------

                        // ...

                        int i = 0;
                        Transaction t;
                        
                        // search running transaction list for right transaction
                        do
                        {
                            t = runningTransactions.get(i);
                        }
                        while(t.transactionID != (int)message.getContent());

                        // use lock manager to release all locks
                        TransactionServer.lockManager.unLock(t);

                        // remove it from running transactions and move it to committed transactions
                        runningTransactions.remove(t);

                        // send message to client that transaction committed
                        committedTransactions.add(t);

                        break;

                    // -------------------------------------------------------------------------------------------
                    case READ_REQUEST:
                    // -------------------------------------------------------------------------------------------

                        // ...

                        // get content of message
                        int accountNum = (int)message.getContent();

                        // log it   
                        
                        // TODO where does "transaction" variable come from?
                        
                        try {
                            // ==================================================================>
                            balance = TransactionServer.accountManager.read(accountNumber, transaction);
                            // <==================================================================
                            
                            // ...

                            // send read request response back to client
                                // send back transaction ID?? TODO
                            writeToNet.writeObject(new Message(READ_REQUEST_RESPONSE, transaction.transactionID));
                                                    
                        } catch (TransactionAbortedException ex) {

                            // ...

                            // write before image to accounts
                            transaction.beforeImage.forEach( (a, b) ->
                            
                               { TransactionServer.accountManager.getAccount(a)._write(b); } 

                            );

                            // low-level write to the accounts what the balance was before

                            // release all acquired locks (lock manager)
                            TransactionServer.lockManager.unLock(transaction);

                            // send message to client stating it aborted
                                // TODO idk what to return here...
                            writeToNet.writeObject(new Message(TRANSACTION_ABORTED, transaction.transactionID));

                            // close streams
                            client.close();
                            
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

                            // send write request response back to client
                                // TODO again what do i return here... as the message content
                            writeToNet.writeObject(new Message(WRITE_REQUEST_RESPONSE, transaction.transactionID));
                            

                        } catch (TransactionAbortedException ex) {

                            // ...

                            // write before image to accounts
                            transaction.beforeImage.forEach( (a, b) ->
                            
                                { TransactionServer.accountManager.getAccount(a)._write(b); }

                            );

                            // low-level write to the accounts what the balance was before

                            // release all acquired locks (lock manager)
                            TransactionServer.lockManager.unLock(transaction);

                            // send message to client stating it aborted
                                // TODO same problem...
                            writeToNet.writeObject(new Message(TRANSACTION_ABORTED, transaction.transactionID));

                            // close streams
                            client.close();
                        }


                        break;

                    // -------------------------------------------------------------------------------------------
                    case SHUTDOWN:
                        // -------------------------------------------------------------------------------------------

                        // client sent shutdown message, tell the server
                        TransactionServer.shutDown();

                        // bail out
                        keepgoing = false;

                        continue;

                    // -------------------------------------------------------------------------------------------
                    default:
                        // -------------------------------------------------------------------------------------------

                        System.out.println("[TransactionManagerWorker.run] Warning: Message type not implemented");
                }
            }
        }
    }
}
