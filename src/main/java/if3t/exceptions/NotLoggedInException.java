package if3t.exceptions;

public class NotLoggedInException extends Exception 
{
	private static final long serialVersionUID = -2316965296840926233L;

	public NotLoggedInException() {
		super("ERROR: not logged in!");
	}
	
	public NotLoggedInException(String message) {
		super(message);
	}
}
