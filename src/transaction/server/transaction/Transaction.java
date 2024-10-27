package transaction.server.transaction;

import java.util.ArrayList;
import java.util.HashMap;
import transaction.server.TransactionServer;
import transaction.server.lock.Lock;

/**
 * Class representing transactions
 * 
 * @author wolfdieterotte
 */
public class Transaction {

    int transactionID;
    ArrayList<Lock> locks = null;
    HashMap <Integer, Integer> beforeImage;
    
    StringBuffer log = new StringBuffer("");
    
    Transaction(int transactionID) {
        this.transactionID = transactionID;
        this.locks = new ArrayList();
        this.beforeImage =  new HashMap();
    }
    
    
    public int getTransactionID() {
        return transactionID;
    }
    
    
    public ArrayList<Lock> getLocks() {
        return locks;
    }
    
    
    public void addLock(Lock lock) {
        locks.add(lock);
    }
    
    
    public HashMap getBeforeImage()
    {
        return beforeImage;
    }
    
    
    public void addBeforeImage(int account, int balance)
    {
        beforeImage.put(account, balance);
        
        this.log("[Transaction.addBeforeImage]   | set before image for account #" + account + " to $" + balance); 
    }

    
    public void log (String logString) {
        
        int messageCount = TransactionServer.getMessageCount();
        
        log.append("\n").append(messageCount).append(" ").append(logString);
        
        if (!TransactionServer.transactionView) {
            System.out.println(messageCount + " Transaction #" + transactionID + " " + logString);            
        }
    }
    
    
    public StringBuffer getLog() {
        return log;
    }
}
