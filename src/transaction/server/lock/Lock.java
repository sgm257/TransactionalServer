package transaction.server.lock;

import java.util.ArrayList;
import java.util.Iterator;
import transaction.server.transaction.Transaction;
import transaction.server.account.Account;

/**
 *
 * @author wolfdieterotte
 */
public class Lock implements LockTypes {

    // the account this lock protects
    private final Account account;

    // the current lock type
    private int currentLockType;

    // the current lock holders
    private final ArrayList<Transaction> lockHolders;

    // the current lock requestors
    private final ArrayList<Transaction> lockRequestors;

    // for logging purposes
    private static String preFixLogString = "[Lock.acquire]                 |";

    /**
     * Constructor
     *
     * @param account
     */
    public Lock(Account account) {
        this.account = account;

        this.lockHolders = new ArrayList();
        this.lockRequestors = new ArrayList();

        this.currentLockType = EMPTY_LOCK;
    }

    /**
     * One of the two key methods, used to acquire a lock on an account Calling
     * this method on a lock, it is understood that the lock is attached to a
     * certain account
     *
     * @param transaction Transaction trying to set lock
     * @param newLockType The lock type to be set
     *
     * @throws TransactionAbortedException Exception that will be thrown when a
     * deadlock is detected
     */
    public synchronized void acquire(Transaction transaction, int newLockType) throws TransactionAbortedException {
        transaction.log(preFixLogString
                + " try to set " + getLockTypeString(newLockType)
                + " on account #" + account.getNumber());

        // begin conflict loop >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
        while (isConflict(transaction, newLockType)) {
            // the lock this transaction tries to set is conflicting, so it is about getting blocked
            // if other transactions wait for a lock that this one holds, we may have a deadlock
            // in this case we rather abort this transaction

            // run through all the locks that this transaction has
            // look at each lock's lock requestors. if not empty, this transaction needs to abort
            // ...
            
            lockRequestors.add(transaction);

            try {
                // start sleeping >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
                // ============================
                wait();
                // ============================
                // woke up <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

            } catch (InterruptedException e) {
                // ignore ...
            }

            lockRequestors.remove(transaction);

        }
        // end conflict loop <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

        // save the before image in transaction, if not already there
        if (currentLockType != WRITE_LOCK && newLockType == WRITE_LOCK) {
            // ...
        }

        // set the lock - implementation of pseudocode from the book
        // nobody holding the lock, just go ahead
        if (lockHolders.isEmpty()) {
            // ...
        } // this transaction is not one of the transactions already holding this lock, so it can only be a shared lock
        else if (!lockHolders.contains(transaction)) {
            // another transaction holds the lock (i.e. read lock, otherwise we would not have got here)
            // so just share the (read) lock
            // ...
        } // when the above two checks fail, this transaction is a lock holder
        // we now check if the transaction is the sole lock holder and if the lock needs to be promoted        
        else if (currentLockType == READ_LOCK && newLockType == WRITE_LOCK) {
            // ...
        } // if all the above didn't fire, we are in either of three cases:
        // - this transaction tries to set a read  lock on a read  lock it holds - possibly with other transactions
        // - this transaction tries to set a read  lock on a write lock it holds
        // - this transaction tries to set a write lock on a write lock it holds
        // all three cases are ignored as there is no need to do anything   
        else {
            // nothing to do
        }
    }

    /**
     * The second of the two key methods, called to release a lock on an account
     * This is the counterpart to acquire()
     *
     * @param transaction
     */
    public synchronized void release(Transaction transaction) {
        lockHolders.remove(transaction); // remove this holder

        if (lockHolders.isEmpty()) {
            currentLockType = EMPTY_LOCK;

            if (lockRequestors.isEmpty()) {
                // this lock is not used any more, we could delete it
                // but, in a heavily loaded system it will be recreated quickly,
                // so we don't, for performance reasons
            }
        }

        // this is the counter operation to the wait() call in acquire()
        // wake up all transactions waiting on this lock to be released
        // ============================
        notifyAll();
        // ============================
    }

    /**
     * Helper method used in acquire() to check if a lock to be set on the
     * account conflicts with lock(s) of other transactions
     *
     * @param transaction
     * @param newLockType
     * @return
     */
    private boolean isConflict(Transaction transaction, int newLockType) {
        // ...
    }

    /**
     * Helper method to return the current lock type
     *
     * @return
     */
    public synchronized int getLockType() {
        return currentLockType;
    }

    /**
     * Helper method to return the account this lock is attached to
     *
     * @return the account this lock is attached to
     */
    public Account getAccount() {
        return account;
    }

    /**
     * Helper method to return a convenient string pertaining to the lock
     * symbolic constant
     *
     * @param lockType
     * @return the string pertaining to the lock type
     */
    public static String getLockTypeString(int lockType) {
        String lockString = "Locktype not implemented";
        switch (lockType) {
            case READ_LOCK:
                lockString = "READ_LOCK";
                break;
            case WRITE_LOCK:
                lockString = "WRITE_LOCK";
                break;
            case EMPTY_LOCK:
                lockString = "EMPTY_LOCK";
                break;
        }
        return lockString;
    }
}
