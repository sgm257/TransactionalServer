package transaction.server.lock;

import java.util.HashMap;
import transaction.server.account.Account;
import java.util.Iterator;
import transaction.server.transaction.Transaction;

/**
 *
 * @author wolfdieterotte
 */
public class LockManager implements LockTypes 
{

    private static HashMap<Account, Lock> locks;
    private static boolean applyLocking;

    public LockManager(boolean applyLocking) 
    {
        locks = new HashMap<>();
        LockManager.applyLocking = applyLocking;
    }

    
    public void lock(Account account, Transaction transaction, int lockType) throws TransactionAbortedException
    {
        // return, if we don't do locking
        if (!applyLocking) return;
        
        // get the lock that is attached to this account
        Lock lock;
        synchronized (this) 
        {
            lock = locks.get(account);

            if (lock == null) 
            {
                // there is no lock attached to this account, create one
                lock = new Lock(account);
                locks.put(account, lock);

                transaction.log("[LockManager.setLock]          | lock created, account #" + account.getNumber());
            }
        }
     // ------------------------------------
        lock.acquire(transaction, lockType);
     // ------------------------------------
    }

    
    public synchronized void unLock(Transaction transaction) 
    {
        // return, if we don't do locking
        if (!applyLocking) return;
        
        Iterator<Lock> lockIterator = transaction.getLocks().listIterator();
        Lock currentLock;
        while (lockIterator.hasNext()) 
        {
            currentLock = lockIterator.next();
            transaction.log("[LockManager.unLock]           | release " + Lock.getLockTypeString(currentLock.getLockType()) + ", account #" + currentLock.getAccount().getNumber());
         // ---------------------------------
            currentLock.release(transaction);
         // ---------------------------------

            //if (/* lock is empty and lock requestors  is empty */) {
                // remove the lock as it is not needed any more
                // not implemented
            //}
        }
    }
    
    
    public HashMap<Account, Lock> getLocks() 
    {
        return locks;
    }
}
