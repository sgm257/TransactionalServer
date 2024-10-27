package transaction.server.account;

import java.util.HashMap;
import transaction.server.transaction.Transaction;
import transaction.server.TransactionServer;
import transaction.server.lock.LockTypes;
import transaction.server.lock.TransactionAbortedException;

/**
 *
 * @author wolfdieterotte
 */
public class AccountManager implements LockTypes
{
    
    private static HashMap<Integer, Account> accounts;
    private static int numberAccounts;
    private static int initialBalance;
    
    public AccountManager(int numberAccounts, int initialBalance) 
    {
        accounts = new HashMap();
        AccountManager.numberAccounts = numberAccounts;
        AccountManager.initialBalance = initialBalance;
        
        for (int i = 0; i < numberAccounts; i++) 
        {
            accounts.put(i, new Account(i, initialBalance));
        }
    }
    
    
    public Account getAccount(int accountNumber) 
    {
        return accounts.get(accountNumber);
    }
    
    
    public HashMap<Integer, Account> getAccounts()
    {
        return accounts;
    }
    
    
    public int read (int accountNumber, Transaction transaction) throws TransactionAbortedException
    {
        // get the account
        Account account = getAccount(accountNumber);
        
        // set the read lock
        (TransactionServer.lockManager).lock(account, transaction, READ_LOCK);
        
        // the above call will likely wait (if not deadlock), until it continues here
        return (getAccount(accountNumber))._read();
    }
    
    
    public void write (int accountNumber, Transaction transaction, int balance) throws TransactionAbortedException
    {
        // get the account
        Account account = getAccount(accountNumber);

        // set the write lock
        (TransactionServer.lockManager).lock(account, transaction, WRITE_LOCK);
        
        // above call may wait (or deadlock), until it continues here
        account._write(balance);
    }
}
