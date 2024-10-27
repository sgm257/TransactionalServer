
package transaction.server.lock;

/**
 * Interface [LockTypes] Defines the different lock types used in the application.
 * Any entity using objects of class Lock needs to implement this interface.
 * 
 * @author Dr.-Ing. Wolf-Dieter Otte
 */
public interface LockTypes 
{
    
    public static final int EMPTY_LOCK = 1;
    public static final int READ_LOCK  = 2;
    public static final int WRITE_LOCK = 3;
}
