package transaction.server.account;

/**
 *
 * @author wolfdieterotte
 */
public class Account 
{
    
    private int balance;
    private final int number;
    
    public Account (int number, int initialBalance) 
    {
        this.balance = initialBalance;
        this.number = number;
    }
    
    public int _read() 
    {
        return balance;
    }
    
    public void _write(int balance) 
    {
        this.balance = balance;
    }
    
    public int getNumber() 
    {
        return number;
    }    
}
