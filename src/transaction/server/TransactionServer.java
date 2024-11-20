package transaction.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import transaction.server.account.AccountManager;
import transaction.server.account.Account;
import transaction.server.lock.LockManager;
import transaction.server.transaction.TransactionManager;
import transaction.server.transaction.Transaction;
import utils.PropertyHandler;


/**
 * Class that represents a transaction server
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public class TransactionServer implements Runnable
{
    // manager objects
    public static AccountManager     accountManager     = null;
    public static TransactionManager transactionManager = null;
    public static LockManager        lockManager        = null;
    
    // the server socket to accept incoming clients' requests
    static ServerSocket serverSocket = null;
    
    // flag indicating to keep running the server loop
    static boolean keepgoing = true;
    
    // flag for logging purposes. decides to either show logs from a transaction's perspective
    // or reflecting the program execution
    public static boolean transactionView;

    // unique counter to number log message, so they can be ordered how they occurred
    static int messageCounter = 0;

    
    /**
     * Constructor
     * 
     * @param serverPropertiesFile file containing server-side configuration information
     */
    public TransactionServer(String serverPropertiesFile) 
    {

        Properties serverProperties = null;

        // variables holding config information
        int numberAccounts;
        int initialBalance;

        // get properties
        try 
        {
            serverProperties = new PropertyHandler(serverPropertiesFile);
        } catch (IOException e) 
        {
            System.out.println("[TransactionServer.TransactionServer] Didn't find properties file \"" + serverPropertiesFile + "\"");
            System.exit(1);
        }

        // create transaction manager
        transactionView = Boolean.parseBoolean(serverProperties.getProperty("TRANSACTION_VIEW"));
        TransactionServer.transactionManager = new TransactionManager();
        System.out.println("[TransactionServer.TransactionServer] TransactionManager created");

        // create lock manager
        boolean applyLocking = Boolean.parseBoolean(serverProperties.getProperty("APPLY_LOCKING"));
        TransactionServer.lockManager = new LockManager(applyLocking);
        System.out.println("[TransactionServer.TransactionServer] LockManager created");

        // create account manager
        numberAccounts = Integer.parseInt(serverProperties.getProperty("NUMBER_ACCOUNTS"));
        initialBalance = Integer.parseInt(serverProperties.getProperty("INITIAL_BALANCE"));
        
        TransactionServer.accountManager = new AccountManager(numberAccounts, initialBalance);
        System.out.println("[TransactionServer.TransactionServer] AccountManager created");

        // create server socket
        try 
        {
            serverSocket = new ServerSocket(Integer.parseInt(serverProperties.getProperty("PORT")));
            System.out.println("[TransactionServer.TransactionServer] ServerSocket created on port: " + serverSocket.getLocalPort());
        } catch (IOException ex) 
        {
            System.err.println("[TransactionServer.TransactionServer] Could not create server socket");
            System.exit(1);
        }
    }

    
    /**
     * Helper method to return a system-wide, unique counter for logging purposes
     * 
     * @return the counter value
     */
    public static synchronized int getMessageCount()
    {
        return ++messageCounter;
    }
    
    
    /**
     * Helper method called indirectly by client by sending a SHUTDOWN message to server,
     * which is received by a TransactionManagerWorker that in turn will call this method.
     * 
     * The method is static to avoid passing on this server's object reference
     */
    public static void shutDown()
    {
        try {
            keepgoing = false;
            serverSocket.close();
        } catch (IOException ex) {
            Logger.getLogger(TransactionServer.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
  
    
    /**
     * Print out summary
     */
    public void printOutSummary()
    {        
        System.out.println("\n\n======================================= COMMITTED TRANSACTIONS INFORMATION =======================================");

        StringBuffer committedTransactionsLogs = new StringBuffer();
        Iterator<Transaction> committedTransactionsIterator = TransactionServer.transactionManager.getCommittedTransactions().iterator();
        Transaction committedTransaction;

        while (committedTransactionsIterator.hasNext())
        {
            committedTransaction = committedTransactionsIterator.next();
            committedTransactionsLogs.append(committedTransaction.getLog()).append("\n");
        }

        System.out.print(committedTransactionsLogs);
        //*/
        
        
        System.out.println("\n\n======================================= BRANCH TOTAL =======================================");

        HashMap<Integer, Account> accounts = TransactionServer.accountManager.getAccounts();
        int total = 0;


        for (Map.Entry<Integer, Account> entry : accounts.entrySet()) { 
            total += entry.getValue()._read();
        }

        // according to NetBeans, the above can be replaced by the below monster
        // - are you kidding me? If this is not bad, what is? WTF!!!
        // total = accounts.entrySet().stream().map(entry -> entry.getValue()._read()).reduce(total, Integer::sum);

        System.out.println("---> $" + total);
        
        System.out.println("\n\n======================================= ACCOUNT BALANCES =======================================");

        for (Map.Entry<Integer, Account> entry : accounts.entrySet()) { 
                    System.out.print(entry.getValue()._read() + " ");
        }
       
        System.out.println("\n\n");
        
        System.exit(0);
}

    /**
     * Run the server loop
     */
    @Override
    public void run() 
    {
        // run server loop
        while (keepgoing) 
        {
            // run server loop
            try 
            {
                transactionManager.runTransaction(serverSocket.accept());
            } 
            catch (SocketException e) 
            {
                // we get here when we close the socket from the outside
                System.out.println("[TransactionServer.run] Socket closed, shutting down ...");
            } 
            catch (IOException ex) 
            {
                System.out.println("IOException in server loop");
            }
        }

        // we are done with the server loop, wait a second for things to calm down
        try 
        {
            Thread.sleep(1000);
        } 
        catch (InterruptedException ex) 
        {
            // ignore
        }        
        
        // print out summary
        printOutSummary();
        
        System.exit(1); // make sure to really exit if the application is interrupted from the client side
    }
    
    
    /**
     * main()
     * @param args 
     */
    public static void main(String[] args)
    {
        if (args.length == 1) {
            new TransactionServer(args[0]).run();
        } else {
            new TransactionServer("/root/distributedSystems/TransactionalServer/src/config/TransactionServer.properties").run();
        }
    }
}
