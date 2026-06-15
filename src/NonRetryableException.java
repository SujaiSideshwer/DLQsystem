//thrown by handler when a failure is permanent and retry is pointless
public class NonRetryableException extends Exception{
    public NonRetryableException(String message){
        super(message);
    }
}
